// 用 Chrome DevTools Protocol 精确统计「菜单点击 / 页面挂载」触发的后端请求次数
// 依赖：Node 22 内置 WebSocket（无第三方依赖）
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
    if (!page) { console.error('NO_PAGE_TARGET'); process.exit(1); }

    const ws = new WebSocket(page.webSocketDebuggerUrl);
    let id = 0;
    const pending = new Map();
    let reqs = [];
    const consoleLogs = [];

    ws.onmessage = ev => {
        const m = JSON.parse(ev.data);
        if (m.id && pending.has(m.id)) { pending.get(m.id)(m); pending.delete(m.id); }
        if (m.method === 'Network.requestWillBeSent') {
            const u = m.params.request.url;
            // 只保留真正的后端接口调用（前端模块路径 /src/api/xxx.ts 要排除）
            if (u.startsWith(BASE + '/api/')) reqs.push(u.replace(BASE, ''));
        }
        if (m.method === 'Runtime.consoleAPICalled' && m.params.type === 'log') {
            consoleLogs.push(m.params.args.map(a => a.value === undefined ? a.type : a.value).join(' '));
        }
    };
    await new Promise(r => { ws.onopen = r; });
    const send = (method, params = {}) => new Promise(res => {
        const i = ++id; pending.set(i, res);
        ws.send(JSON.stringify({ id: i, method, params }));
    });
    const evalJs = async expr => {
        const r = await send('Runtime.evaluate', { expression: expr, returnByValue: true, awaitPromise: true });
        if (r.result && r.result.exceptionDetails) return 'ERR:' + JSON.stringify(r.result.exceptionDetails.text);
        return r.result && r.result.result && r.result.result.value;
    };

    await send('Network.enable');
    await send('Page.enable');
    await send('Runtime.enable');

    // 1) 写入完整登录态
    await send('Page.navigate', { url: BASE + '/login' });
    await sleep(2500);
    const userStorage = JSON.stringify({
        state: { user: { id: 1, username: 'admin', nickname: '系统管理员', avatar: null }, token: TOKEN },
        version: 0
    });
    await evalJs(`localStorage.setItem('token', ${JSON.stringify(TOKEN)});
                  localStorage.setItem('user-storage', ${JSON.stringify(userStorage)});
                  localStorage.removeItem('menu-storage'); 'ok';`);

    const report = (title, list) => {
        console.log('\n--- ' + title + ' ---');
        const counter = {};
        list.forEach(u => counter[u.split('?')[0]] = (counter[u.split('?')[0]] || 0) + 1);
        Object.entries(counter).forEach(([u, n]) => console.log(`  ${n} ×  ${u}`));
        console.log('  合计: ' + list.length);
    };

    // ===== 测试 A：直接整页打开「品牌管理」列表页 =====
    reqs = [];
    await send('Page.navigate', { url: BASE + '/basedata/brand' });
    await sleep(6000);
    const urlA = await evalJs('location.pathname');
    report('测试A｜整页加载 /basedata/brand（当前路径 ' + urlA + '）', reqs.slice());

    // ===== 测试 B：SPA 点击侧边栏菜单「单位」 =====
    const expand = await evalJs(`
      (() => {
        // 展开所有折叠的子菜单
        document.querySelectorAll('.ant-menu-submenu-title').forEach(e => {
          if (e.getAttribute('aria-expanded') !== 'true') e.dispatchEvent(new MouseEvent('click', { bubbles: true }));
        });
        return document.querySelectorAll('.ant-menu-title-content').length;
      })()
    `);
    await sleep(1200);
    reqs = [];
    const clicked = await evalJs(`
      (() => {
        const el = [...document.querySelectorAll('.ant-menu-title-content')].find(e => e.textContent.trim().includes('供应商管理'));
        if (!el) return 'NOT_FOUND(已有标签: ' + [...document.querySelectorAll('.ant-menu-title-content')].map(e=>e.textContent.trim()).join('|') + ')';
        el.closest('li').dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));
        return el.textContent.trim();
      })()
    `);
    await sleep(5000);
    const urlB = await evalJs('location.pathname');
    report('测试B｜点击菜单「' + clicked + '」→ 当前路径 ' + urlB + '（展开后菜单项数 ' + expand + '）', reqs.slice());

    // ===== 测试 C：再点一次「品牌」回到列表页（第二次挂载，排除首次特例） =====
    reqs = [];
    const clicked2 = await evalJs(`
      (() => {
        const el = [...document.querySelectorAll('.ant-menu-title-content')].find(e => e.textContent.trim().includes('品牌'));
        if (!el) return 'NOT_FOUND';
        el.closest('li').dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));
        return el.textContent.trim();
      })()
    `);
    await sleep(5000);
    const urlC = await evalJs('location.pathname');
    report('测试C｜再点菜单「' + clicked2 + '」→ 当前路径 ' + urlC + '（第二次进入同一页）', reqs.slice());

    // ===== 测试 D：在同一页点「查询/刷新」类操作，看是否叠加 =====
    reqs = [];
    const searched = await evalJs(`
      (() => {
        const btn = [...document.querySelectorAll('button')].find(b => b.textContent.includes('查询') || b.textContent.includes('搜索'));
        if (!btn) return 'NO_BUTTON';
        btn.click();
        return btn.textContent.trim();
      })()
    `);
    await sleep(3500);
    report('测试D｜点击「' + searched + '」按钮', reqs.slice());

    console.log('\n=== 页面 console 片段（检查 StrictMode 双渲染痕迹）===');
    consoleLogs.filter(l => l && (l.includes('🔍') || l.includes('menus'))).slice(0, 10)
        .forEach(l => console.log('  ' + String(l).slice(0, 90)));

    ws.close();
    process.exit(0);
}

main().catch(e => { console.error('ERR', e); process.exit(1); });
