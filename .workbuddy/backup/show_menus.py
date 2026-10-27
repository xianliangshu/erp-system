import json

for uid, name in ((2, '采购经理'), (3, '仓库管理员')):
    with open(f'.workbuddy/backup/r_u{uid}.json', encoding='utf-8') as f:
        d = json.load(f)
    tree = d.get('data') or []

    def count(ns):
        t = 0
        for n in ns:
            t += 1 + count(n.get('children') or [])
        return t

    print(f'===== userId={uid} ({name})  code={d.get("code")}  顶级目录={len(tree)}  节点总数={count(tree)} =====')

    def walk(ns, depth=1):
        for n in ns:
            ch = n.get('children') or []
            kind = '目录' if n['menuType'] == 0 else ('菜单' if n['menuType'] == 1 else '按钮')
            print('   ' * depth + f"{n['code']} {n['title']} [{kind}] 子={len(ch)}")
            walk(ch, depth + 1)

    walk(tree)
    print()
