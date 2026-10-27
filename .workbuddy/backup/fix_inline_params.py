#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""补齐：内联 api 调用里的 { current, size: pageSize, ... } → { ...query, ... }"""
import os, re, shutil

SRC = r"D:\code\java\java_study\erp-system\erp-adminPro\src\pages"
FILES = [
    r"Business\Settle\CustomerFeeSheet\List\index.tsx",
    r"Business\Sale\Delivery\List\index.tsx",
    r"Business\Settle\CustomerCheckSheet\List\index.tsx",
    r"Business\Sale\Return\List\index.tsx",
    r"Business\Settle\CheckSheet\List\index.tsx",
    r"Business\Settle\SettleSheet\List\index.tsx",
    r"Business\Settle\CustomerPreSheet\List\index.tsx",
    r"Business\Settle\CustomerSettleSheet\List\index.tsx",
    r"Business\Stock\Check\List\index.tsx",
]

total = 0
for rel in FILES:
    p = os.path.join(SRC, rel)
    s = open(p, encoding='utf-8').read()
    before = s
    s, n1 = re.subn(r'\(\{ current, size: pageSize,', r'({ ...query,', s)
    s, n2 = re.subn(r'\(\{ current, size: pageSize \}', r'({ ...query }', s)
    n = n1 + n2
    assert n >= 1, f"未命中: {rel}"
    assert not re.search(r'size: pageSize|\{ current,', s), f"仍有残留: {rel}"
    open(p, 'wb').write(s.encode('utf-8'))
    total += n
    print(f"  ✓ {rel}  替换 {n} 处")
print(f"\n合计替换 {total} 处")
