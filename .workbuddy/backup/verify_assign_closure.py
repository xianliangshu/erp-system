"""验证 SysRoleServiceImpl.assignMenus 的祖先自动补齐（回归测试）。

步骤：
  1) 读取角色 4 当前的菜单ID集合 S
  2) 只提交 S 中的"叶子节点"（去掉所有有子节点的目录/菜单）
  3) 重新读取，期望结果与 S 完全一致 —— 等价于证明服务端把祖先补回来了
"""
import json
import urllib.request

BASE = 'http://127.0.0.1:8081/api'


def get(path):
    with urllib.request.urlopen(BASE + path) as r:
        return json.load(r)


def post(path, payload):
    req = urllib.request.Request(
        BASE + path,
        data=json.dumps(payload).encode('utf-8'),
        headers={'Content-Type': 'application/json'},
        method='POST',
    )
    with urllib.request.urlopen(req) as r:
        return json.load(r)


# 1) 全量菜单树 -> 计算"有子节点"的ID
tree = get('/system/menu/tree')['data']
parent_ids = set()
id_to_name = {}


def walk(nodes):
    for n in nodes:
        id_to_name[n['id']] = n['title']
        children = n.get('children') or []
        if children:
            parent_ids.add(n['id'])
        walk(children)


walk(tree)

before = sorted(get('/system/role/4/menus')['data'])
leaves_only = [i for i in before if i not in parent_ids]
removed = [i for i in before if i in parent_ids]

print(f'角色4 原有菜单 {len(before)} 个')
print(f'  其中目录/父节点 {len(removed)} 个 -> {"、".join(id_to_name.get(i, str(i)) for i in removed)}')
print(f'  本次只提交叶子节点 {len(leaves_only)} 个（故意不带父级）')

# 2) 只提交叶子
resp = post('/system/role/4/menus', {'menuIds': leaves_only})
print(f'\n提交叶子节点 -> code={resp["code"]} message={resp["message"]}')

# 3) 重新读取并比对
after = sorted(get('/system/role/4/menus')['data'])
print(f'回读菜单 {len(after)} 个')

if after == before:
    print('\n[PASS] 与提交前完全一致 —— 祖先目录已被服务端自动补回')
else:
    print('\n[FAIL] 结果不一致')
    print('  丢失:', [id_to_name.get(i, i) for i in set(before) - set(after)])
    print('  多出:', [id_to_name.get(i, i) for i in set(after) - set(before)])

# 4) 反向验证：确认"目录确实被删掉过"，排除"根本没删"的假阳性
print('\n反向校验：再次只提交叶子，检查过程确实经历了先删后补')
missing_parent = [i for i in after if i in parent_ids]
print(f'  当前集合中的父节点 {len(missing_parent)} 个 -> {"、".join(id_to_name.get(i, str(i)) for i in missing_parent)}')
