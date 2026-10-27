// 验证「分页后再点搜索」是否重复请求（useEffect 依赖 + 手动调用叠加）
const http = require('http');
const TOKEN = process.argv[2];
const BASE = 'http://localhost:3000';

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
    let id = 0; const pending = new Map(); let reqs = [];

    ws.onmessage = ev => {
        const m = JSON.parse(ev.data);
        if (m.id && pending.has(m.id)) { pending.get(m.id)(m); pending.delete(m.id); }
        if (m.method === 'Network.requestWillBeSent') {
            const u = m.params.request.url;
            if (u.startsWith(BASE + '/api/')) reqs.push(u.replace(BASE, ''));
        }
    };
    await new Promise(r => { ws.onopen = r; });
    const send = (method, params = {}) => new Promise(res => { const i = ++id; pending.set(i, res); ws.send(JSON.stringify({ id: i, method, params })); });
    const evalJs = async expr => {
        const r = await send('Runtime.evaluate', { expression: expr, returnByValue: true, awaitPromise: true });
        if (r.result && r.result.exceptionDetails) return 'ERR:' + r.result.exceptionDetails.text;
        return r.result && r.result.result && r.result.result.value;
    };
    await send('Network.enable'); await send('Page.enable'); await send('Runtime.enable');

    await send('Page.navigate', { url: BASE + '/login' });
    await sleep(2500);
    const us = JSON.stringify({ state: { user: { id: 1, username: 'admin', nickname: '系统管理员', avatar: null }, token: TOKEN }, version: 0 });
    await evalJs(`localStorage.setItem('token', ${JSON.stringify(TOKEN)}); localStorage.setItem('user-storage', ${JSON.stringify(us)}); localStorage.removeItem('menu-storage'); 'ok';`);

    const report = (t, list) => {
        console.log('\n--- ' + t + ' ---');
        list.forEach((u, i) => console.log('  ' + (i + 1) + '. ' + u));
        console.log('  合计: ' + list.length);
    };

    // 进列表页
    reqs = [];
    await send('Page.navigate', { url: BASE + '/basedata/brand' });
    await sleep(6000);
    report('① 进入品牌列表页（首屏，current=1）', reqs.slice());

    // 点第 2 页
    reqs = [];
    const p2 = await evalJs(`
      (() => { const li = document.querySelector('.ant-pagination-item-2');
        if (!li) return 'NO_PAGE2'; li.click(); return 'clicked page2'; })()
    `);
    await sleep(3500);
    report('② 点分页「第 2 页」（' + p2 + '）', reqs.slice());

    // 点搜索（此时 current=2）
    reqs = [];
    const btn = await evalJs(`
      (() => {
        const b = [...document.querySelectorAll('button')].find(x => x.textContent.includes('搜'));
        if (!b) return 'NO_BTN(' + [...document.querySelectorAll('button')].map(x=>x.textContent.trim()).join('|') + ')';
        b.click(); return 'clicked 搜索';
      })()
    `);
    await sleep(3500);
    report('③ 在「第 2 页」上点搜索（' + btn + '）← 关键', reqs.slice());

    // 点重置（同样 current!==1）
    reqs = [];
    const btn2 = await evalJs(`
      (() => {
        const b = [...document.querySelectorAll('button')].find(x => x.textContent.includes('重'));
        if (!b) return 'NO_BTN'; b.click(); return 'clicked 重置';
      })()
    `);
    await sleep(3500);
    report('④ 在「第 2 页」上点重置（' + btn2 + '）', reqs.slice());

    ws.close(); process.exit(0);
}
main().catch(e => { console.error('ERR', e); process.exit(1); });
