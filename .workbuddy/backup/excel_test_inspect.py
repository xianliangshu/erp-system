"""检查导出的 xlsx 表头与数据行（临时验证脚本）"""
import openpyxl
import sys

for path in sys.argv[1:]:
    wb = openpyxl.load_workbook(path)
    ws = wb.active
    print(f"===== {path}  sheet={ws.title}  dims={ws.dimensions} =====")
    rows = list(ws.iter_rows(values_only=True))
    if not rows:
        print("  (空)")
        continue
    print("  HEADER:", rows[0])
    for r in rows[1:]:
        print("  ROW   :", r)
    print()
