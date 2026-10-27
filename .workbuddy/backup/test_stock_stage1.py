"""库存体系补齐 —— 第一段（S1~S3）后端接口实测脚本

覆盖：盘点参数读写 / 库存预警 CRUD 与实时命中 / 库存成本调整单审核调价 / 权限

用法：
    python test_stock_stage1.py [token]

不传 token 时自动用 admin/123456 登录获取。
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
            return e.code, {"raw": raw}


def login():
    st, r = call("POST", "/auth/login", None, {"username": "admin", "password": "123456"})
    token = (r.get("data") or {}).get("token")
    if not token:
        print("登录失败：", r)
        sys.exit(1)
    return token


def check(name, ok, detail=""):
    (PASS if ok else FAIL).append(name)
    print(("  [OK]   " if ok else "  [FAIL] ") + name + (("  -> " + str(detail)) if detail else ""))


def num(v):
    return float(v) if v is not None else 0.0


def summary():
    print("=" * 70)
    print(f"通过 {len(PASS)} 项，失败 {len(FAIL)} 项")
    if FAIL:
        print("失败项：")
        for f in FAIL:
            print("  - " + f)
    return 0 if not FAIL else 1


def get_stock(token, sc_id=None):
    q = f"/business/stock/page?current=1&size=10" + (f"&scId={sc_id}" if sc_id else "")
    st, r = call("GET", q, token)
    records = (r.get("data") or {}).get("records") or []
    return records[0] if records else None


def main():
    token = sys.argv[1] if len(sys.argv) > 1 else login()
    print("=" * 70)

    stock = get_stock(token)
    if not stock:
        print("基线缺失：product_stock 没有数据，无法测试")
        return summary()
    sc_id = stock["scId"]
    product_id = stock["productId"]
    base_num = num(stock["stockNum"])
    base_price = num(stock["taxPrice"])
    print(f"基线：仓库={sc_id} 商品={product_id} 库存={base_num} 成本价={base_price} "
          f"金额={num(stock['taxAmount'])}")
    print("-" * 70)

    # ==================== 用例 1：盘点参数设置 ====================
    print("用例1 盘点参数读取与保存")
    st, r = call("GET", "/stock/take/config", token)
    cfg = r.get("data") or {}
    check("盘点参数接口可读", r.get("code") == 200 and cfg, r.get("message"))
    check("默认 cancelHours=24", cfg.get("cancelHours") == 24, cfg.get("cancelHours"))
    check("默认 autoChangeStock=0（安全默认，不自动调账）", cfg.get("autoChangeStock") == 0,
          cfg.get("autoChangeStock"))

    st, r = call("PUT", "/stock/take/config", token,
                 {"cancelHours": 48, "autoChangeStock": 1, "showProduct": 0,
                  "showStock": 1, "allowChangeNum": 0})
    check("保存盘点参数成功", r.get("code") == 200, r.get("message"))

    st, r = call("GET", "/stock/take/config", token)
    cfg2 = r.get("data") or {}
    check("参数已持久化（cancelHours=48）", cfg2.get("cancelHours") == 48, cfg2.get("cancelHours"))
    check("参数已持久化（autoChangeStock=1）", cfg2.get("autoChangeStock") == 1, cfg2.get("autoChangeStock"))
    check("参数已持久化（allowChangeNum=0）", cfg2.get("allowChangeNum") == 0, cfg2.get("allowChangeNum"))

    # 恢复默认
    call("PUT", "/stock/take/config", token,
         {"cancelHours": 24, "autoChangeStock": 0, "showProduct": 1,
          "showStock": 1, "allowChangeNum": 1})
    st, r = call("GET", "/stock/take/config", token)
    check("参数可恢复默认", (r.get("data") or {}).get("autoChangeStock") == 0, r.get("data"))
    print("-" * 70)

    # ==================== 用例 2：库存预警 ====================
    print("用例2 库存预警配置与实时命中")
    low_limit = base_num + 10
    st, r = call("POST", "/stock/warning", token,
                 {"scId": sc_id, "productId": product_id,
                  "minLimit": low_limit, "status": 1, "remark": "接口测试"})
    check("新增预警配置成功", r.get("code") == 200, r.get("message"))

    st, r = call("GET", "/stock/warning/page?current=1&size=10", token)
    records = (r.get("data") or {}).get("records") or []
    warn = next((x for x in records if x.get("productId") == product_id and x.get("scId") == sc_id), None)
    check("预警配置出现在列表", warn is not None, len(records))
    if warn is None:
        return summary()
    check("列表回填商品名称", bool(warn.get("productName")), warn.get("productName"))
    check("列表回填仓库名称", bool(warn.get("scName")), warn.get("scName"))
    check("列表回填当前库存", num(warn.get("stockNum")) == base_num, warn.get("stockNum"))
    warn_id = warn["id"]

    st, r = call("GET", f"/stock/warning/hit-list?scId={sc_id}", token)
    hits = r.get("data") or []
    mine = [h for h in hits if h.get("productId") == product_id]
    check(f"库存 {base_num} 低于下限 {low_limit} → 命中", len(mine) == 1, mine)
    check("命中类型为「低于下限」(hitType=1)", mine and mine[0].get("hitType") == 1,
          mine[0].get("hitType") if mine else None)

    safe_limit = base_num - 10
    st, r = call("PUT", "/stock/warning", token,
                 {"id": warn_id, "scId": sc_id, "productId": product_id,
                  "minLimit": safe_limit, "status": 1})
    check("修改预警下限成功", r.get("code") == 200, r.get("message"))
    st, r = call("GET", f"/stock/warning/hit-list?scId={sc_id}", token)
    mine = [h for h in (r.get("data") or []) if h.get("productId") == product_id]
    check(f"下限调为 {safe_limit} 后不再命中", len(mine) == 0, mine)

    st, r = call("POST", "/stock/warning", token,
                 {"scId": sc_id, "productId": product_id, "minLimit": 1, "status": 1})
    check("同仓库同商品重复新增被拦截", r.get("code") != 200, r.get("message"))

    st, r = call("PUT", "/stock/warning", token,
                 {"id": warn_id, "scId": sc_id, "productId": product_id,
                  "maxLimit": base_num - 10, "status": 1})
    st, r = call("GET", f"/stock/warning/hit-list?scId={sc_id}", token)
    mine = [h for h in (r.get("data") or []) if h.get("productId") == product_id]
    check("上限低于库存 → 命中「高于上限」(hitType=2)",
          len(mine) == 1 and mine[0].get("hitType") == 2, mine)

    st, r = call("POST", "/stock/warning", token,
                 {"scId": sc_id, "productId": product_id, "status": 1})
    check("上下限全空被拦截", r.get("code") != 200, r.get("message"))

    st, r = call("POST", "/stock/warning", token,
                 {"scId": sc_id, "productId": product_id, "minLimit": 100, "maxLimit": 10, "status": 1})
    check("下限大于上限被拦截", r.get("code") != 200, r.get("message"))
    print("-" * 70)

    # ==================== 用例 3：库存成本调整单 ====================
    print("用例3 库存成本调整单：审核生效")
    new_price = base_price + 40
    st, r = call("POST", "/stock/adjust/cost", token,
                 {"scId": sc_id, "description": "接口测试-审核通过",
                  "details": [{"productId": product_id, "price": new_price}]})
    check("新增成本调整单成功", r.get("code") == 200, r.get("message"))
    sheet_id = ((r.get("data") or {}).get("id"))
    check("返回单据ID", sheet_id is not None, r.get("data"))

    st, r = call("GET", f"/stock/adjust/cost/{sheet_id}", token)
    sheet = r.get("data") or {}
    d = (sheet.get("details") or [{}])[0]
    check("明细回填调整前成本价（服务端快照）", num(d.get("oriPrice")) == base_price, d.get("oriPrice"))
    check("明细回填库存数量（服务端快照）", num(d.get("stockNum")) == base_num, d.get("stockNum"))
    expected_diff = (new_price - base_price) * base_num
    check("明细差额计算正确", abs(num(d.get("diffAmount")) - expected_diff) < 0.01,
          f"{d.get('diffAmount')} vs {expected_diff}")
    check("单据状态为待审核", sheet.get("status") == 0, sheet.get("status"))

    after_create = get_stock(token, sc_id)
    check("审核前成本价未变", num(after_create["taxPrice"]) == base_price, after_create["taxPrice"])

    st, r = call("POST", f"/stock/adjust/cost/approve/{sheet_id}", token)
    check("审核通过成功", r.get("code") == 200, r.get("message"))

    after = get_stock(token, sc_id)
    check(f"审核后成本价更新为 {new_price}", num(after["taxPrice"]) == new_price, after["taxPrice"])
    check("审核后库存数量保持不变（关键）", num(after["stockNum"]) == base_num, after["stockNum"])
    expect_amount = new_price * base_num
    check("审核后库存金额已同步", abs(num(after["taxAmount"]) - expect_amount) < 0.01,
          f"{after['taxAmount']} vs {expect_amount}")

    st, r = call("GET", "/business/stock/log/page?current=1&size=20&bizType=13", token)
    logs = (r.get("data") or {}).get("records") or []
    cost_logs = [x for x in logs if x.get("bizId") == sheet_id]
    check("生成 COST_ADJUST(13) 库存流水", len(cost_logs) >= 1, len(logs))
    if cost_logs:
        check("流水数量变动为 0（未改数量）", num(cost_logs[0].get("stockNum")) == 0, cost_logs[0].get("stockNum"))

    st, r = call("POST", f"/stock/adjust/cost/approve/{sheet_id}", token)
    check("已审核单据不能重复审核", r.get("code") != 200, r.get("message"))

    st, r = call("DELETE", f"/stock/adjust/cost/{sheet_id}", token)
    check("审核通过的单据不能删除", r.get("code") != 200, r.get("message"))
    print("-" * 70)

    print("用例4 库存成本调整单：审核拒绝不改库存")
    st, r = call("POST", "/stock/adjust/cost", token,
                 {"scId": sc_id, "description": "接口测试-审核拒绝",
                  "details": [{"productId": product_id, "price": 999}]})
    check("新增第二张单据成功", r.get("code") == 200, r.get("message"))
    sheet2 = ((r.get("data") or {}).get("id"))

    st, r = call("POST", f"/stock/adjust/cost/refuse/{sheet2}", token, {"refuseReason": "接口测试拒绝"})
    check("审核拒绝成功", r.get("code") == 200, r.get("message"))

    refused = get_stock(token, sc_id)
    check("拒绝后成本价不变", num(refused["taxPrice"]) == new_price, refused["taxPrice"])
    check("拒绝后库存数量不变", num(refused["stockNum"]) == base_num, refused["stockNum"])

    st, r = call("GET", f"/stock/adjust/cost/{sheet2}", token)
    check("拒绝原因已落库", (r.get("data") or {}).get("refuseReason") == "接口测试拒绝",
          (r.get("data") or {}).get("refuseReason"))
    print("-" * 70)

    print("用例5 异常与权限")
    st, r = call("POST", "/stock/adjust/cost", token,
                 {"scId": sc_id, "details": [{"productId": product_id, "price": base_price}]})
    dup_sheet = ((r.get("data") or {}).get("id"))
    st, r = call("POST", "/stock/adjust/cost", token,
                 {"scId": sc_id,
                  "details": [{"productId": product_id, "price": 1}, {"productId": product_id, "price": 2}]})
    check("明细中商品重复被拦截", r.get("code") != 200, r.get("message"))

    st, r = call("POST", "/stock/adjust/cost", token, {"scId": sc_id, "details": []})
    check("空明细被拦截", r.get("code") != 200, r.get("message"))

    st, r = call("POST", "/stock/adjust/cost", token,
                 {"scId": sc_id, "details": [{"productId": 99999999, "price": 1}]})
    check("不存在的商品被拦截", r.get("code") != 200, r.get("message"))

    st, r = call("GET", "/stock/warning/page", None)
    check("无 token 访问预警接口返回 401", st == 401, st)

    st, r = call("GET", "/stock/adjust/cost/page", None)
    check("无 token 访问成本调整接口返回 401", st == 401, st)

    # 清理可删的单据
    if dup_sheet:
        call("DELETE", f"/stock/adjust/cost/{dup_sheet}", token)
    if sheet2:
        call("DELETE", f"/stock/adjust/cost/{sheet2}", token)
    call("DELETE", f"/stock/warning/{warn_id}", token)

    print(f"（已通过的调价单 #{sheet_id} 与库存成本价恢复需用 SQL 清理）")
    return summary()


if __name__ == "__main__":
    sys.exit(main())
