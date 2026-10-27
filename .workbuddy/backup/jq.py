import sys
import json

"""极简 JSON 取值助手：stdin 读 JSON，argv[1] 是求值表达式（变量 d = 解析后的对象）。
例：echo '{"a":1}' | python jq.py "d['a']"  →  1
"""
raw = sys.stdin.read()
d = json.loads(raw) if raw.strip() else {}
expr = sys.argv[1]
try:
    print(eval(expr))
except Exception as e:  # noqa: BLE001
    print(f"__ERR__{e}")
    sys.exit(1)
