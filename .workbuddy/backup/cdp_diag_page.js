// 诊断某个页面为何没有发起取数请求
const http = require('http');
const TOKEN = process.argv[2];
const PATH_ = process.argv[3] || '/system/loginlog';
const BASE = 'http://localhost:3001';

function getJSON(url) {
    return new Promise((res, rej) => {
        http.get(url, r => { let d = ''; r.on('data', c => d += c); r.on('end', () => res(JSON.parse(d))); }).on('error', rej);
    });
}
const sleep = ms => new Promise(r => setTimeout(r, ms));

async function main() {
    const targets = await getJSON('http://127.0.0.1:9222/json');
    const page = targets.find(t => t.type === 'page');
    const ws = new WebSocket(page.webSocketDebuggerUrl);
    let id = 0; const pending = new Map(); const reqs = []; const errs = [];

    ws.onmessage = ev => {
        const m = JSON.parse(ev.data);
        if (m.id && pending.has(m.id)) { pending.get(m.id)(m); pending.delete(m.id); }
        if (m.method === 'Network.requestWillBeSent') {
            const u = m.params.request.url;
            if (u.startsWith(BASE + '/api/')) reqs.push(u.replace(BASE, ''));
        }
        if (m.method === 'Runtime.exceptionThrown') {
            const d = m.params.exceptionDetails;
            errs.push((d.exception && d.exception.description) || d.text);
        }
        if (m.method === 'Runtime.consoleAPICalled' && m.params.type === 'error') {
            errs.push('[console.error] ' + m.params.args.map(a => a.value ?? a.description).join(' '));
        }
    };
    await new Promise(r => { ws.onopen = r; });
    const send = (method, params = {}) => new Promise(res => { const i = ++id; pending.set(i, res); ws.send(JSON.stringify({ id: i, method, params })); });
    const evalJs = async expr => {
        const r = await send('Runtime.evaluate', { expression: expr, returnByValue: true, awaitPromise: true });
        return r.result && r.result.result && r.result.result.value;
    };
    await send('Network.enable'); await send('Page.enable'); await send('Runtime.enable');

    await send('Page.navigate', { url: BASE + '/login' });
    await sleep(2500);
    const us = JSON.stringify({ state: { user: { id: 1, username: 'admin', nickname: '系统管理员', avatar: null }, token: TOKEN }, version: 0 });
    await evalJs(`localStorage.setItem('token', ${JSON.stringify(TOKEN)}); localStorage.setItem('user-storage', ${JSON.stringify(us)}); localStorage.removeItem('menu-storage'); 'ok';`);

    reqs.length = 0; errs.length = 0;
    await send('Page.navigate', { url: BASE + PATH_ });
    await sleep(9000);

    console.log('目标路径:', PATH_);
    console.log('当前 location:', await evalJs('location.href'));
    console.log('\n--- 网络请求 (api) ---');
    reqs.forEach((u, i) => console.log('  ' + (i + 1) + '. ' + u));
    console.log('  合计: ' + reqs.length);
    console.log('\n--- 运行时错误 ---');
    errs.slice(0, 5).forEach((e, i) => console.log('  ' + (i + 1) + '. ' + String(e).slice(0, 400)));
    if (!errs.length) console.log('  (无)');
    console.log('\n--- 页面可见文本片段 ---');
    console.log('  ' + String(await evalJs('document.body.innerText')).replace(/\n/g, ' | ').slice(0, 600));
    console.log('\n--- 表格行数 / 按钮 ---');
    console.log('  tbody tr =', await evalJs("document.querySelectorAll('.ant-table-tbody tr').length"));
    console.log('  buttons  =', await evalJs("[...document.querySelectorAll('button')].map(x=>x.textContent.replace(/\\s/g,'')).join('|')"));

    ws.close(); process.exit(0);
}
main().catch(e => { console.error('ERR', e); process.exit(1); });
