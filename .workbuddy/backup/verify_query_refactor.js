// 验证「列表页取数收敛」改造效果：翻页后点搜索/重置，是否还会重复请求
// 用法：node verify_query_refactor.js <token>
const http = require('http');
const TOKEN = process.argv[2];
const BASE = 'http://localhost:3001';

const PAGES = [
    ['/business/sale/order', '销售订单（紧凑单行风格 · 12 条）'],
    ['/business/stock/check', '盘点单（内联调用变体 · 只测首屏）'],
];

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
        return r.result && r.result.result && r.result.result.value;
    };
    await send('Network.enable'); await send('Network.setCacheDisabled', { cacheDisabled: true }); await send('Page.enable'); await send('Runtime.enable');

    // 预置登录态
    await send('Page.navigate', { url: BASE + '/login' });
    await sleep(2500);
    const us = JSON.stringify({ state: { user: { id: 1, username: 'admin', nickname: '系统管理员', avatar: null }, token: TOKEN }, version: 0 });
    await evalJs(`localStorage.setItem('token', ${JSON.stringify(TOKEN)}); localStorage.setItem('user-storage', ${JSON.stringify(us)}); localStorage.removeItem('menu-storage'); 'ok';`);

    const listOnly = () => reqs.filter(u => /\/page\?|\/list\?|\/page$/.test(u) || /current=/.test(u));
    let fail = 0;
    const check = (label, list, expect) => {
        const ok = list.length === expect;
        if (!ok) fail++;
        console.log(`   ${ok ? 'PASS' : 'FAIL'}  ${label}  期望${expect}次 → 实际${list.length}次`);
        list.forEach((u, i) => console.log(`         ${i + 1}. ${u}`));
    };

    for (const [path, title] of PAGES) {
        console.log('\n' + '='.repeat(84));
        console.log('页面：' + title + '   ' + path);
        console.log('='.repeat(84));

        reqs = [];
        await send('Page.navigate', { url: BASE + path });
        await sleep(8000);
        check('① 首屏挂载', listOnly().slice(), 1);

        reqs = [];
        const p2 = await evalJs(`(() => { const li = document.querySelector('.ant-pagination-item-2');
            if (!li) return 'NO_PAGE2'; li.click(); return 'ok'; })()`);
        await sleep(3000);
        if (p2 === 'NO_PAGE2') { console.log('   SKIP  数据不足 2 页，无法测试翻页场景'); continue; }
        check('② 点「第 2 页」', listOnly().slice(), 1);

        reqs = [];
        const btn = await evalJs(`(() => {
            const btns = [...document.querySelectorAll('button')];
            const norm = t => t.replace(/\\s/g, '');
            const b = btns.find(x => /查询|搜索/.test(norm(x.textContent)));
            if (!b) return 'NO_BTN[' + btns.map(x => norm(x.textContent)).join('|') + ']';
            b.click(); return norm(b.textContent); })()`);
        await sleep(3000);
        console.log(`   （点击按钮：${btn}）`);
        check('③ 在第 2 页点搜索 ← 关键（改造前为 2 次，且含 current=2）', listOnly().slice(), 1);

        reqs = [];
        const btn2 = await evalJs(`(() => {
            const btns = [...document.querySelectorAll('button')];
            const norm = t => t.replace(/\\s/g, '');
            const b = btns.find(x => /重置/.test(norm(x.textContent)));
            if (!b) return 'NO_BTN';
            b.click(); return norm(b.textContent); })()`);
        await sleep(3000);
        check('④ 在第 2 页点重置', listOnly().slice(), 1);
    }

    console.log('\n' + '='.repeat(84));
    console.log(fail === 0 ? '✔ 全部通过：搜索/重置/翻页各自只发 1 次请求' : `✘ 有 ${fail} 项未通过`);
    console.log('='.repeat(84));
    ws.close(); process.exit(fail === 0 ? 0 : 2);
}
main().catch(e => { console.error('ERR', e); process.exit(1); });
