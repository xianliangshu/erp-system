#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
把列表页的取数逻辑收敛到单个 query state，消除「显式 loadData + useEffect」叠加双发与竞态。

改造前：
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const params = { current, size: pageSize, name: searchName || undefined };
    useEffect(() => { loadData(); }, [current, pageSize]);
    const handleSearch = () => { setCurrent(1); loadData(); };

改造后：
    const [query, setQuery] = useState<any>({ current: 1, size: 10 });
    const params = { ...query, name: searchName || undefined };
    useEffect(() => { loadData(); }, [query]);        // 唯一取数触发源
    const handleSearch = () => setQuery(q => ({ ...q, current: 1 }));   // 新对象 ⇒ 必触发

用法：python refactor_list_pages.py [--apply]
"""
import os, re, sys, shutil, codecs

SRC_ROOT = r"D:\code\java\java_study\erp-system\erp-adminPro\src"
BACKUP = r"D:\code\java\java_study\erp-system\.workbuddy\backup\list_page_orig"
TSV = r"D:\code\java\java_study\erp-system\.workbuddy\backup\list_page_scan.tsv"
APPLY = '--apply' in sys.argv

# ---------- 目标文件：effect 依赖里含 current 的列表页 ----------
targets = []
for ln in open(TSV, encoding='utf-8').read().splitlines()[1:]:
    c = ln.split('\t')
    if len(c) >= 5 and 'current' in c[4]:
        targets.append(c[0])

TAB = []
def add(name, pat, rep, required=True, flags=0):
    TAB.append((name, re.compile(pat, flags), rep, required))

# P1  分页 state → query state
add('P1-state',
    r'(?P<i>[ \t]*)const \[current, setCurrent\] = useState\((?P<c>\d+)\);[ \t]*\r?\n'
    r'(?P<i2>[ \t]*)const \[pageSize, setPageSize\] = useState\((?P<s>\d+)\);',
    lambda m: (f"{m.group('i')}// 分页与查询条件：唯一的取数依据，搜索/重置/翻页都只改它\r\n"
               f"{m.group('i2')}const [query, setQuery] = useState<any>({{ current: {m.group('c')}, size: {m.group('s')} }});"))

# P2  参数表头：current / size: pageSize  →  ...query
add('P2-params',
    r'(?P<i>[ \t]*)current,[ \t]*\r?\n(?P<i2>[ \t]*)size: pageSize,[ \t]*\r?\n',
    lambda m: f"{m.group('i')}...query,\r\n")

# P2b 参数表头（单行紧凑写法）
add('P2b-params1', r'const params = \{[ \t]*current, size: pageSize,', r'const params = { ...query,')
add('P2b-params2', r'const params = \{[ \t]*current, size: pageSize[ \t]*\}', r'const params = { ...query }')
add('P2b-params3',
    r'(?P<i>[ \t]*)current, size: pageSize,[ \t]*\r?\n',
    lambda m: f"{m.group('i')}...query,\r\n")

# P3  effect 依赖
add('P3-deps', r'\},[ \t]*\[current, pageSize\]\)', r'}, [query])')
add('P3b-deps-tab', r'\[activeTab, current, pageSize\]', r'[activeTab, query]')

# P4  搜索 / 重置里的  setCurrent(1); loadData();  →  setQuery(...)
add('P4-setcur',
    r'(?P<i>[ \t]*)setCurrent\(1\);[ \t]*\r?\n[ \t]*loadData\([^)]*\);[ \t]*\r?\n',
    lambda m: f"{m.group('i')}setQuery((q: any) => ({{ ...q, current: 1 }}));\r\n")
# P4b 单行紧凑写法
add('P4b-setcur1', r'(?<![.\w])setCurrent\(1\); *loadData\([^)]*\);',
    r'setQuery((q: any) => ({ ...q, current: 1 }));')
# P4c 用 setTimeout 绕过闭包过期的写法 → 不再需要
add('P4c-setcur-timer',
    r'(?P<i>[ \t]*)setCurrent\(1\);[ \t]*\r?\n[ \t]*setTimeout\(loadData, *0\);[ \t]*\r?\n',
    lambda m: f"{m.group(1)}setQuery((q: any) => ({{ ...q, current: 1 }}));\r\n")

# P5  分页器 current/pageSize（以 total, 为锚，避免误伤）
add('P5-pager',
    r'(?P<i>[ \t]*)current,[ \t]*\r?\n(?P<i2>[ \t]*)pageSize,[ \t]*\r?\n(?P<i3>[ \t]*)total,',
    lambda m: (f"{m.group('i')}current: query.current,\r\n"
               f"{m.group('i2')}pageSize: query.size,\r\n"
               f"{m.group('i3')}total,"))
# P5b 单行分页器
add('P5b-pager1', r'(?<![.\w])current, pageSize, total,', r'current: query.current, pageSize: query.size, total,')

# P6  分页器 onChange（多行 / 单行两种）
add('P6-onchange',
    r'(?P<i>[ \t]*)onChange: \(page, size\) => \{[ \t]*\r?\n[ \t]*setCurrent\(page\);[ \t]*\r?\n[ \t]*setPageSize\(size\);[ \t]*\r?\n[ \t]*\},?',
    lambda m: f"{m.group('i')}onChange: (page, size) => setQuery((q: any) => ({{ ...q, current: page, size }})),")
add('P6b-onchange', r'onChange: \(page, size\) => \{[ \t]*setCurrent\(page\);[ \t]*setPageSize\(size\);[ \t]*\},?',
    r'onChange: (page, size) => setQuery((q: any) => ({ ...q, current: page, size })),')

# P7  LoginLog 变体：loadData(page = current, size = pageSize) → loadData()
add('P7-loadsig',
    r'const loadData = async \(page = current, size = pageSize\) => \{',
    r'const loadData = async () => {', required=False)

# ---------- 残留检查：改完不允许再出现这些符号 ----------
LEFTOVER = ['setCurrent', 'setPageSize']

results = []
for rel in targets:
    path = os.path.join(SRC_ROOT, rel.replace('/', os.sep))
    raw = open(path, 'rb').read()
    src = raw.decode('utf-8')
    orig = src
    counts = {}
    for name, pat, rep, req in TAB:
        src, n = pat.subn(rep, src)
        counts[name] = n

    left = {s: len(re.findall(s, src)) for s in LEFTOVER}
    ok = True
    notes = []
    # 每组必须命中的最少次数（组内任一变体命中即算，取加总）
    GROUPS = (
        ('分页state', ('P1-state',), 1),
        ('effect依赖', ('P3-deps', 'P3b-deps-tab'), 1),
        ('搜索/重置', ('P4-setcur', 'P4b-setcur1', 'P4c-setcur-timer'), 2),
        ('分页器current', ('P5-pager', 'P5b-pager1'), 1),
        ('分页器onChange', ('P6-onchange', 'P6b-onchange'), 1),
    )
    for gname, keys, least in GROUPS:
        c = sum(counts.get(k, 0) for k in keys)
        if c < least:
            ok = False
            notes.append(f'缺 {gname}({c}<{least})')
    if any(left.values()):
        ok = False
        notes.append('残留 ' + ','.join(f'{k}x{v}' for k, v in left.items() if v))

    results.append((rel, ok, counts, notes))

    if ok and APPLY:
        bp = os.path.join(BACKUP, rel.replace('/', os.sep))
        os.makedirs(os.path.dirname(bp), exist_ok=True)
        if not os.path.exists(bp):
            shutil.copy2(path, bp)
        open(path, 'wb').write(src.encode('utf-8'))

good = [r for r in results if r[1]]
bad = [r for r in results if not r[1]]
print("=" * 90)
print(f"{'APPLIED' if APPLY else 'DRY-RUN'}  可自动改造 {len(good)} / 共 {len(results)} 个文件")
print("=" * 90)
for rel, ok, counts, notes in good:
    print("  OK   %s" % rel.replace('pages/', ''))
print()
if bad:
    print("-" * 90)
    print("需人工处理的 %d 个文件：" % len(bad))
    for rel, ok, counts, notes in bad:
        print("  !!   %-52s %s" % (rel.replace('pages/', ''), ' ; '.join(notes)))
    print()
    print("各 pattern 命中数：P1=%s P2=%s P3=%s P4=%s P5=%s P6=%s P6b=%s P7=%s" % tuple(
        sum(r[2].get(k, 0) for r in results) for k in
        ('P1-state', 'P2-params', 'P3-deps', 'P4-setcur', 'P5-pager', 'P6-onchange', 'P6b-onchange', 'P7-loadsig')))
