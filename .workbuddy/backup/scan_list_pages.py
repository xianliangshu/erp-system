#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""扫描前端列表页的取数结构，输出聚类报表。"""
import os, re, json, collections

ROOT = r"D:\code\java\java_study\erp-system\erp-adminPro\src"

files = []
for dirpath, dirnames, filenames in os.walk(ROOT):
    if 'node_modules' in dirpath:
        continue
    for fn in filenames:
        if fn.endswith('.tsx'):
            files.append(os.path.join(dirpath, fn))

rows = []
for f in files:
    src = open(f, encoding='utf-8').read()
    if 'loadData' not in src and 'load(' not in src:
        continue
    has_effect_dep = bool(re.search(r'useEffect\(', src))
    # 统计特征
    n_setcurrent1 = len(re.findall(r'setCurrent\(1\)', src))
    # useEffect 依赖数组
    deps = re.findall(r'\}\s*,\s*\[([^\]]*)\]\s*\)', src)
    # 显式 loadData() 调用次数（排除函数定义行）
    n_call = len(re.findall(r'(?<![a-zA-Z])loadData\(\);', src))
    has_form = 'Form.useForm()' in src
    # 搜索 state 名
    search_states = re.findall(r'const \[(search[A-Za-z]+|query[A-Za-z]+)', src)
    rows.append({
        'f': os.path.relpath(f, ROOT).replace('\\', '/'),
        'setcur1': n_setcurrent1,
        'deps': deps,
        'calls': n_call,
        'form': has_form,
        'searchStates': search_states,
        'size': len(src.splitlines()),
    })

# 输出
out = r"D:\code\java\java_study\erp-system\.workbuddy\backup\list_page_scan.tsv"
with open(out, 'w', encoding='utf-8') as fh:
    fh.write("file\tsetcur1\tcalls\tform\tdeps\tsearchStates\tsize\n")
    for r in rows:
        fh.write("%s\t%d\t%d\t%s\t%s\t%s\t%d\n" % (
            r['f'], r['setcur1'], r['calls'], r['form'],
            '|'.join(r['deps'])[:120], ','.join(r['searchStates']), r['size']))

print("scanned files:", len(rows))
print("\n=== 依赖数组分布 ===")
for k, v in collections.Counter('|'.join(r['deps']) for r in rows).most_common(20):
    print("%3d  %s" % (v, k[:110]))
print("\n=== search state 数分布 ===")
for k, v in collections.Counter(len(r['searchStates']) for r in rows).most_common():
    print("  %d 个搜索state: %d 个文件" % (k, v))
print("\n=== 无 setCurrent(1) 的文件 ===")
for r in rows:
    if r['setcur1'] == 0:
        print("  %s (deps=%s)" % (r['f'], '|'.join(r['deps'])[:80]))
