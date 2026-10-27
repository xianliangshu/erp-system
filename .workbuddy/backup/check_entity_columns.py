# -*- coding: utf-8 -*-
"""交叉校验：Java 实体继承关系 -> 需要的公共列 vs 数据库实际列"""
import os
import re
import glob

ROOT = 'D:/code/java/java_study/erp-system'

# 基类 -> 该基类带来的公共列
BASE_COLS = {
    'BaseBusinessEntity': ['id', 'create_time', 'update_time', 'create_by', 'update_by', 'deleted'],
    'BaseDataEntity':     ['id', 'create_time', 'update_time', 'create_by', 'update_by', 'deleted'],
    'BaseSystemEntity':   ['id', 'create_time', 'update_time', 'create_by', 'update_by', 'is_deleted'],
    'BaseAuditEntity':    ['id', 'create_time', 'update_time', 'create_by', 'update_by'],
}

# 数据库实际列
actual = {}
with open(os.path.join(ROOT, '.workbuddy/backup/all_columns.tsv'), encoding='utf-8') as f:
    for line in f:
        line = line.rstrip('\n').replace('\\t', '\t')
        if '\t' not in line:
            continue
        t, c = line.split('\t', 1)
        actual.setdefault(t.strip(), set()).add(c.strip())

# 解析实体
rows = []
files = []
for mod in ['erp-common', 'erp-system', 'erp-basedata', 'erp-business', 'erp-web']:
    files += glob.glob(f'{ROOT}/{mod}/src/main/java/com/erp/**/entity/**/*.java', recursive=True)

for path in files:
    src = open(path, encoding='utf-8', errors='replace').read()
    m_cls = re.search(r'public\s+class\s+(\w+)\s+extends\s+(\w+)', src)
    if not m_cls:
        continue
    cls, base = m_cls.groups()
    if base not in BASE_COLS:
        continue
    m_tbl = re.search(r'@TableName\(\s*(?:value\s*=\s*)?"([^"]+)"', src)
    table = m_tbl.group(1) if m_tbl else cls
    if table.startswith('${') or 'dynamic' in table.lower():
        continue
    need = BASE_COLS[base]
    have = actual.get(table)
    if have is None:
        rows.append((table, cls, base, 'TABLE_MISSING', '', path))
        continue
    # 实体里显式声明为 exist=false 的字段跳过（简单处理）
    missing = [c for c in need if c not in have]
    if missing:
        rows.append((table, cls, base, 'COL_MISSING', ','.join(missing), path))

print(f'扫描实体文件 {len(files)} 个，问题 {len(rows)} 处')
print('=' * 100)
if rows:
    print(f"{'表名':<32}{'实体':<26}{'基类':<20}{'缺失列'}")
    print('-' * 100)
    for table, cls, base, kind, missing, path in sorted(rows):
        label = missing if kind == 'COL_MISSING' else '<整张表不存在>'
        print(f'{table:<32}{cls:<26}{base:<20}{label}')
else:
    print('全部一致')
