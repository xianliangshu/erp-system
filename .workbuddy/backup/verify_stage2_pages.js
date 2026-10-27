// 第二段前端页面实测：盘点任务页 / 预先盘点单页 / 预先盘点单编辑页
//   ① 每页首屏只发 1 次列表请求（收敛写法）
//   ② 无控制台异常、无未捕获错误
//   ③ 关键交互链路可点通（新增任务弹窗 / 明细抽屉 / 预先盘点单表单）
// 用法：node verify_stage2_pages.js <token>
const http = require('http');
const TOKEN = process.argv[2];
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
    let id = 0; const pending = new Map();
    let reqs = []; let errors = [];

    ws.onmessage = ev => {
        const m = JSON.parse(ev.data);
        if (m.id && pending.has(m.id)) { pending.get(m.id)(m); pending.delete(m.id); }
        if (m.method === 'Network.requestWillBeSent') {
            const u = m.params.request.url;
            if (u.startsWith(BASE + '/api/')) reqs.push(u.replace(BASE, ''));
        }
        if (m.method === 'Runtime.exceptionThrown') {
            errors.push('[uncaught] ' + JSON.stringify(m.params.exceptionDetails.exception?.description || m.params.exceptionDetails.text));
        }
        if (m.method === 'Runtime.consoleAPICalled' && m.params.type === 'error') {
            errors.push('[console.error] ' + (m.params.args || []).map(a => a.value || a.description || '').join(' '));
        }
    };
    await new Promise(r => { ws.onopen = r; });
    const send = (method, params = {}) => new Promise(res => { const i = ++id; pending.set(i, res); ws.send(JSON.stringify({ id: i, method, params })); });
    const evalJs = async expr => {
        const r = await send('Runtime.evaluate', { expression: expr, returnByValue: true, awaitPromise: true });
        if (r.result && r.result.exceptionDetails) return 'EVAL_ERR:' + JSON.stringify(r.result.exceptionDetails.text);
        return r.result && r.result.result && r.result.result.value;
    };
    await send('Network.enable');
    await send('Network.setCacheDisabled', { cacheDisabled: true });
    await send('Page.enable'); await send('Runtime.enable');

    await send('Page.navigate', { url: BASE + '/login' });
    await sleep(2500);
    const us = JSON.stringify({ state: { user: { id: 1, username: 'admin', nickname: '系统管理员', avatar: null }, token: TOKEN }, version: 0 });
    await evalJs(`localStorage.setItem('token', ${JSON.stringify(TOKEN)}); localStorage.setItem('user-storage', ${JSON.stringify(us)}); localStorage.removeItem('menu-storage'); 'ok';`);

    let fail = 0;
    const check = (label, actual, expect) => {
        const okv = String(actual) === String(expect);
        if (!okv) fail++;
        console.log(`   ${okv ? 'PASS' : 'FAIL'}  ${label}   期望=${expect} 实际=${actual}`);
    };
    const listReqs = () => reqs.filter(u => /\/page\?|\/page$/.test(u));

    const goto = async (path, wait = 7000) => {
        reqs = []; errors = [];
        await send('Page.navigate', { url: BASE + path });
        await sleep(wait);
    };
    const clickByText = (re) => evalJs(`(() => {
        const norm = t => (t || '').replace(/\\s/g, '');
        const btns = [...document.querySelectorAll('button, .ant-btn, a')];
        const b = btns.find(x => new RegExp(${JSON.stringify(re)}).test(norm(x.textContent)));
        if (!b) return 'NO_BTN[' + btns.map(x => norm(x.textContent)).filter(Boolean).slice(0, 30).join('|') + ']';
        b.click(); return norm(b.textContent); })()`);

    const BANNER = t => { console.log('\n' + '='.repeat(84)); console.log(t); console.log('='.repeat(84)); };

    // ---------- 1. 盘点任务页 ----------
    BANNER('页面 1：盘点任务  /stock/take/plan');
    await goto('/stock/take/plan');
    check('① 首屏列表请求次数（收敛写法应为 1）', listReqs().length, 1);
    check('② 表头渲染', await evalJs(`[...document.querySelectorAll('.ant-table-thead th')].map(t=>t.textContent.replace(/\\s/g,'')).join(',')`), '任务编号,仓库,盘点类别,盘点范围,录入进度,状态,备注,创建时间,操作');
    check('③ 行数 > 0（E2E 造的数据）', await evalJs(`document.querySelectorAll('.ant-table-tbody tr.ant-table-row').length`) > 0, true);
    check('④ 无控制台异常', errors.length, 0);
    errors.forEach(e => console.log('        ' + e));

    reqs = [];
    const addR = await clickByText('新增任务');
    await sleep(2000);
    check('⑤ 点「新增任务」后弹窗出现', await evalJs(`!!document.querySelector('.ant-modal-content')`), true);
    check('⑥ 弹窗含「盘点类别」选项', await evalJs(`document.querySelectorAll('.ant-modal-content .ant-form-item').length`), 3);
    await evalJs(`(() => { const b=[...document.querySelectorAll('.ant-modal-content .ant-btn')].find(x=>/取消/.test(x.textContent.replace(/\\s/g,''))); b&&b.click(); return 'ok'; })()`);
    await sleep(800);
    console.log('   （弹窗按钮：' + addR + '）');

    reqs = [];
    const viewR = await clickByText('查看|录入盘点');
    await sleep(3500);
    check('⑦ 点行内「查看/录入盘点」后抽屉打开', await evalJs(`!!document.querySelector('.ant-drawer-content')`), true);
    check('⑧ 抽屉详情请求次数', reqs.filter(u => /\/stock\/take\/plan\/\d+/.test(u)).length >= 1, true);
    check('⑨ 抽屉明细表有列', await evalJs(`document.querySelectorAll('.ant-drawer-content .ant-table-thead th').length`) > 5, true);
    check('⑩ 无控制台异常', errors.length, 0);
    errors.forEach(e => console.log('        ' + e));
    console.log('   （抽屉按钮：' + viewR + '）');

    // ---------- 2. 预先盘点单列表 ----------
    BANNER('页面 2：预先盘点单列表  /stock/take/pre');
    await goto('/stock/take/pre');
    check('① 首屏列表请求次数', listReqs().length, 1);
    check('② 表头渲染', await evalJs(`[...document.querySelectorAll('.ant-table-thead th')].map(t=>t.textContent.replace(/\\s/g,'')).join(',')`), '单据编号,仓库,盘点轮次,备注,创建时间,操作');
    check('③ 行数 > 0', await evalJs(`document.querySelectorAll('.ant-table-tbody tr.ant-table-row').length`) > 0, true);
    check('④ 无控制台异常', errors.length, 0);
    errors.forEach(e => console.log('        ' + e));

    // ---------- 3. 预先盘点单编辑页 ----------
    BANNER('页面 3：预先盘点单新增  /stock/take/pre/add');
    await goto('/stock/take/pre/add');
    check('① 表单渲染（仓库 + 盘点轮次）', await evalJs(`document.querySelectorAll('.ant-form-item').length`) >= 3, true);
    check('② 初始明细为空', await evalJs(`document.querySelectorAll('.ant-table-tbody tr.ant-table-row').length`), 0);
    reqs = [];
    const addBtn = await clickByText('添加商品');
    await sleep(1200);
    check('③ 点「添加商品」后出现 1 行明细', await evalJs(`document.querySelectorAll('.ant-table-tbody tr.ant-table-row').length`), 1);
    check('④ 明细含 3 个数量输入框（初盘/复盘/抽盘）',
        await evalJs(`[...document.querySelectorAll('.ant-table-tbody .ant-input-number')].length`), 3);
    check('⑤ 无控制台异常', errors.length, 0);
    errors.forEach(e => console.log('        ' + e));
    console.log('   （按钮：' + addBtn + '）');

    // ---------- 4. 盘点单表单新增关联下拉 ----------
    BANNER('页面 4：盘点单编辑页的「关联来源」下拉 + 带出商品  /business/stock/check/add');
    await goto('/business/stock/check/add');
    check('① 含「关联盘点任务」表单项',
        await evalJs(`[...document.querySelectorAll('.ant-form-item-label')].map(l=>l.textContent.replace(/\\s/g,'')).includes('关联盘点任务')`), true);
    check('② 含「关联预先盘点单」表单项',
        await evalJs(`[...document.querySelectorAll('.ant-form-item-label')].map(l=>l.textContent.replace(/\\s/g,'')).includes('关联预先盘点单')`), true);
    check('③ 初始明细为空', await evalJs(`document.querySelectorAll('.ant-table-tbody tr.ant-table-row').length`), 0);

    // 打开「关联盘点任务」下拉
    const openSel = await evalJs(`(() => {
        const items=[...document.querySelectorAll('.ant-form-item')];
        const it=items.find(x=>/关联盘点任务/.test(x.textContent.replace(/\\s/g,'')));
        if(!it) return 'NO_ITEM';
        const sel=it.querySelector('.ant-select-selector'); if(!sel) return 'NO_SELECT';
        sel.dispatchEvent(new MouseEvent('mousedown',{bubbles:true}));
        sel.click(); return 'ok'; })()`);
    await sleep(2500);
    const optText = await evalJs(`[...document.querySelectorAll('.ant-select-item-option')].map(o=>o.textContent).join('|')`);
    check('④ 下拉出现待盘点任务选项（含 PD 单号）', /PD/.test(optText || ''), true);
    console.log('   （下拉打开：' + openSel + '；选项：' + String(optText).slice(0, 120) + '）');

    reqs = [];
    const picked = await evalJs(`(() => {
        const o=[...document.querySelectorAll('.ant-select-item-option')].find(x=>/PD/.test(x.textContent));
        if(!o) return 'NO_OPT';
        o.click(); return o.textContent; })()`);
    await sleep(4000);
    check('⑤ 选中任务后请求了任务明细接口',
        reqs.filter(u => /\/stock\/take\/plan\/\d+\/details/.test(u)).length, 1);
    const rowCount = await evalJs(`document.querySelectorAll('.ant-table-tbody tr.ant-table-row').length`);
    check('⑥ showProduct=1 → 商品明细已带出（行数 > 0）', rowCount > 0, true);
    check('⑦ 带出的行已填商品（下拉有值）',
        await evalJs(`document.querySelectorAll('.ant-table-tbody .ant-select-selection-item').length`) > 0, true);
    check('⑧ 无控制台异常', errors.length, 0);
    errors.forEach(e => console.log('        ' + e));
    console.log('   （选中：' + String(picked).slice(0, 60) + '，带出 ' + rowCount + ' 行）');

    console.log('\n' + '='.repeat(84));
    console.log(fail === 0 ? '✔ 全部通过' : `✘ 有 ${fail} 项未通过`);
    console.log('='.repeat(84));
    ws.close(); process.exit(fail === 0 ? 0 : 2);
}
main().catch(e => { console.error('ERR', e); process.exit(1); });
