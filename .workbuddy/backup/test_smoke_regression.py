"""老链路冒烟测试：确认 SKU 改造没有破坏既有接口与数据。

断言口径：HTTP 2xx 且业务 code=200。任一 5xx 或 code!=200 都视为回归。
"""
import json
import sys
import urllib.error
import urllib.request

BASE = "http://localhost:8082/api"
PASS, FAIL = [], []


def call(method, path, token=None, body=None):
    url = BASE + path
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json; charset=utf-8")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req) as resp:
            return resp.status, json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", "replace")
        try:
            return e.code, json.loads(raw)
        except json.JSONDecodeError:
            return e.code, {"raw": raw[:200]}


def check(name, ok, detail=""):
    (PASS if ok else FAIL).append(name)
    print(("  [OK]   " if ok else "  [FAIL] ") + name + (("  -> " + str(detail)) if detail else ""))


def main():
    st, r = call("POST", "/auth/login", None, {"username": "admin", "password": "123456"})
    token = (r.get("data") or {}).get("token")
    if not token:
        print("登录失败", r)
        return 1

    print("=" * 70)
    print("一、基础数据（老接口回归）")
    endpoints = [
        ("物料分页", "/basedata/material/page?current=1&size=5"),
        ("物料下拉", "/basedata/material/list"),
        ("物料详情", "/basedata/material/1"),
        ("分类树", "/basedata/material-category/tree"),
        ("计量单位", "/basedata/unit/list"),
        ("仓库", "/basedata/warehouse/list"),
        ("品牌", "/basedata/brand/list"),
        ("供应商", "/basedata/supplier/list"),
        ("客户", "/basedata/customer/list"),
    ]
    for name, path in endpoints:
        st, r = call("GET", path, token)
        check(name + " 正常", st < 400 and r.get("code") == 200,
              f"http={st}, code={r.get('code')}, msg={r.get('message')}")

    print("二、业务流程（老链路回归）")
    biz = [
        ("采购订单", "/business/purchase/order/page?current=1&size=5"),
        ("采购收货", "/business/purchase/receipt/page?current=1&size=5"),
        ("采购退货", "/business/purchase/return/page?current=1&size=5"),
        ("销售订单", "/business/sale/order/page?current=1&size=5"),
        ("销售出库", "/business/sale/delivery/page?current=1&size=5"),
        ("销售退货", "/business/sale/return/page?current=1&size=5"),
        ("零售出库", "/retail/out/page?current=1&size=5"),
        ("库存查询", "/business/stock/page?current=1&size=5"),
        ("库存流水", "/business/stock/log/page?current=1&size=5"),
        ("库存调整单", "/stock/adjust/sheet/page?current=1&size=5"),
        ("库存盘点", "/business/stock/check/page?current=1&size=5"),
        ("库存调拨", "/business/stock/transfer/page?current=1&size=5"),
        ("供应商费用单", "/business/settle/fee/page?current=1&size=5"),
        ("客户费用单", "/business/settle/customer/fee/page?current=1&size=5"),
    ]
    for name, path in biz:
        st, r = call("GET", path, token)
        check(name + " 正常", st < 400 and r.get("code") == 200,
              f"http={st}, code={r.get('code')}, msg={r.get('message')}")

    print("三、库存数据未被污染")
    st, r = call("GET", "/business/stock/page?current=1&size=20", token)
    recs = (r.get("data") or {}).get("records") or []
    check("库存记录仍存在", len(recs) >= 1, f"{len(recs)} 条")
    if recs:
        first = recs[0]
        check("库存成本价非 0（未被 SKU 改造影响）",
              float(first.get("taxPrice") or 0) > 0, first.get("taxPrice"))

    print("四、老物料仍可正常使用")
    st, r = call("GET", "/basedata/material/page?current=1&size=50&code=MAT000001", token)
    recs = (r.get("data") or {}).get("records") or []
    check("老物料 MAT000001 可查询", len(recs) == 1, len(recs))
    if recs:
        check("老物料已回填所属商品名（迁移 1:1 生成）",
              bool(recs[0].get("spuName")), recs[0].get("spuName"))

    print("=" * 70)
    print(f"通过 {len(PASS)} 项，失败 {len(FAIL)} 项")
    for name in FAIL:
        print("  失败：" + name)
    return 1 if FAIL else 0


if __name__ == "__main__":
    sys.exit(main())
