"""商品中心 SKU 改造 —— 后端接口实测脚本（S2/S3/S4 验收）

用法：
    python test_sku_api.py [token]

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


def main():
    token = sys.argv[1] if len(sys.argv) > 1 else login()
    print("=" * 70)

    # ---------------- 用例 1：新增两个属性组 ----------------
    print("用例1 新增属性组「测试颜色」含 红/蓝")
    st, r = call("POST", "/basedata/sale-property", token,
                 {"name": "测试颜色", "sort": 1,
                  "items": [{"name": "红"}, {"name": "蓝"}]})
    check("新增属性组返回成功", r.get("code") == 200, r.get("message"))

    print("用例2 新增属性组「测试尺码」含 M/L")
    st, r = call("POST", "/basedata/sale-property", token,
                 {"name": "测试尺码", "sort": 2,
                  "items": [{"name": "M"}, {"name": "L"}]})
    check("新增第二个属性组返回成功", r.get("code") == 200, r.get("message"))

    st, r = call("GET", "/basedata/sale-property/list", token)
    props = {p["name"]: p for p in (r.get("data") or [])}
    color = props.get("测试颜色")
    size = props.get("测试尺码")
    check("属性组列表含两个新组", color is not None and size is not None, list(props.keys()))
    if not color or not size:
        return summary()

    color_items = {i["name"]: i["id"] for i in color["items"]}
    size_items = {i["name"]: i["id"] for i in size["items"]}
    color_items_before = dict(color_items)
    check("「测试颜色」自动生成编码", all(i["code"] for i in color["items"]),
          [i["code"] for i in color["items"]])

    # ---------------- 用例 3：SPU + SKU 矩阵 ----------------
    print("用例3 新增 SPU，勾选 颜色(红/蓝) x 尺码(M/L)，预期生成 4 个 SKU")
    payload = {
        "categoryId": 1,
        "unitId": 1,
        "name": "测试商品T恤",
        "specification": "纯棉",
        "selections": [
            {"propertyId": color["id"], "itemIds": [color_items["红"], color_items["蓝"]]},
            {"propertyId": size["id"], "itemIds": [size_items["M"], size_items["L"]]},
        ],
    }
    st, r = call("POST", "/basedata/spu", token, payload)
    spu_id = (r.get("data") or {}).get("spuId")
    check("新增 SPU 成功且返回 spuId", r.get("code") == 200 and spu_id, r)

    if spu_id:
        st, r = call("GET", f"/basedata/spu/{spu_id}/skus", token)
        skus = r.get("data") or []
        check("生成 4 个 SKU", len(skus) == 4, f"实际 {len(skus)} 个")
        texts = sorted(s.get("salePropertyText") or "" for s in skus)
        check("属性文本形如「测试颜色:红 / 测试尺码:M」",
              texts == sorted([
                  "测试颜色:红 / 测试尺码:M", "测试颜色:红 / 测试尺码:L",
                  "测试颜色:蓝 / 测试尺码:M", "测试颜色:蓝 / 测试尺码:L"]), texts)
        check("SKU 编号唯一且非空", len({s["code"] for s in skus}) == 4,
              [s["code"] for s in skus])
        check("SKU 继承 SPU 的分类/单位",
              all(s["categoryId"] == 1 and s["unitId"] == 1 for s in skus))

        st, r = call("GET", f"/basedata/spu/{spu_id}", token)
        detail = r.get("data") or {}
        check("SPU 详情回报 SKU 数量 4", detail.get("skuCount") == 4, detail.get("skuCount"))
        check("SPU 详情回报已绑定属性组 2 个", len(detail.get("propertyIds") or []) == 2)

        # ---------------- 用例 3.5：物料页与 SPU 的关联 ----------------
        print("用例3.5 物料分页返回所属商品名称，并支持按 SPU 筛选")
        st, r = call("GET", f"/basedata/material/page?current=1&size=20&spuId={spu_id}", token)
        page = r.get("data") or {}
        recs = page.get("records") or []
        check("按 spuId 筛选命中 4 条 SKU", len(recs) == 4, len(recs))
        check("物料行回填 spuName",
              bool(recs) and all(x.get("spuName") == "测试商品T恤" for x in recs),
              recs[0].get("spuName") if recs else None)
        check("物料行回填 salePropertyText",
              bool(recs) and all(x.get("salePropertyText") for x in recs),
              recs[0].get("salePropertyText") if recs else None)

        # ---------------- 用例 4：增量生成（只增不删） ----------------
        print("用例4 给「测试颜色」加一个值「白」，重新保存 SPU，预期只新增 2 个 SKU")
        st, r = call("PUT", f"/basedata/sale-property/{color['id']}", token,
                     {"name": "测试颜色", "sort": 1,
                      "items": [{"name": "红"}, {"name": "蓝"}, {"name": "白"}]})
        check("属性组更新成功", r.get("code") == 200, r.get("message"))

        st, r = call("GET", "/basedata/sale-property/list", token)
        props = {p["name"]: p for p in (r.get("data") or [])}
        color_items = {i["name"]: i["id"] for i in props["测试颜色"]["items"]}
        check("原有属性值 ID 保持不变（差异更新未重建）",
              color_items.get("红") == color_items_before.get("红")
              and color_items.get("蓝") == color_items_before.get("蓝"),
              f"before={color_items_before} after={color_items}")
        check("新属性值「白」已生成编码",
              any(i["name"] == "白" and i.get("code") for i in props["测试颜色"]["items"]))

        old_ids = {s["id"] for s in skus}
        payload["selections"][0]["itemIds"] = [color_items["红"], color_items["蓝"], color_items["白"]]
        st, r = call("PUT", f"/basedata/spu/{spu_id}", token, payload)
        check("SPU 更新成功", r.get("code") == 200, r.get("message"))

        st, r = call("GET", f"/basedata/spu/{spu_id}/skus", token)
        skus2 = r.get("data") or []
        check("SKU 总数变为 6", len(skus2) == 6, f"实际 {len(skus2)} 个")
        check("原有 4 个 SKU 未被重建（ID 不变）",
              old_ids.issubset({s["id"] for s in skus2}),
              f"新增 {sorted({s['id'] for s in skus2} - old_ids)}")

        # ---------------- 用例 5：一品多码 ----------------
        print("用例5 一品多码：给第一个 SKU 挂 3 个码（主码 + 2 个条码）")
        target = skus2[0]
        codes = [
            {"code": target["code"], "codeType": 1},
            {"code": "6901234567890", "codeType": 2},
            {"code": "SKU-BAR-0001", "codeType": 2, "remark": "门店自编码"},
        ]
        st, r = call("POST", f"/basedata/material-code/{target['id']}", token, codes)
        check("保存编码成功", r.get("code") == 200, r.get("message"))

        st, r = call("GET", f"/basedata/material-code/list?materialId={target['id']}", token)
        saved = r.get("data") or []
        check("查询到 3 个码", len(saved) == 3, len(saved))
        check("主码被标记 isMain=1",
              sum(1 for c in saved if c.get("isMain") == 1) == 1,
              [(c["code"], c["isMain"]) for c in saved])

        st, r = call("GET", "/basedata/material-code/by-code?code=6901234567890", token)
        hit = r.get("data") or []
        check("按条码扫码命中该 SKU",
              len(hit) == 1 and hit[0]["materialId"] == target["id"], hit)

        st, r = call("GET", f"/basedata/material?page=1&size=1", token)
        st, r = call("GET", f"/basedata/spu/{spu_id}/skus", token)
        flag = [s for s in (r.get("data") or []) if s["id"] == target["id"]]
        check("SKU 的 isMultiCode 被置 1",
              flag and flag[0].get("isMultiCode") == 1, flag[0].get("isMultiCode") if flag else None)

        # ---------------- 用例 6：重复条码拦截 ----------------
        print("用例6 用已被占用的条码挂到另一个 SKU，预期被拒绝")
        other = skus2[1]
        st, r = call("POST", f"/basedata/material-code/{other['id']}", token,
                     [{"code": "6901234567890", "codeType": 2}])
        check("重复条码被拦截",
              r.get("code") != 200 and "占用" in (r.get("message") or ""), r.get("message"))

        # ---------------- 用例 7：删除保护 ----------------
        print("用例7 删除仍有 SKU 的 SPU，预期被拒绝")
        st, r = call("DELETE", f"/basedata/spu/{spu_id}", token)
        check("有 SKU 的 SPU 拒绝删除",
              r.get("code") != 200 and "SKU" in (r.get("message") or ""), r.get("message"))

        print("用例8 删除被 SKU 使用的属性组，预期被拒绝")
        st, r = call("DELETE", f"/basedata/sale-property/{color['id']}", token)
        check("被使用的属性组拒绝删除",
              r.get("code") != 200 and "SKU" in (r.get("message") or ""), r.get("message"))

        print("用例9 删除被 SKU 使用的属性值，预期被拒绝")
        st, r = call("PUT", f"/basedata/sale-property/{color['id']}", token,
                     {"name": "测试颜色", "sort": 1, "items": [{"name": "蓝"}, {"name": "白"}]})
        check("删除被使用的属性值被拒绝",
              r.get("code") != 200 and "使用" in (r.get("message") or ""), r.get("message"))

        print()
        print(f"测试产生的 SPU ID = {spu_id}，属性组 ID = {color['id']} / {size['id']}")

    return summary()


def summary():
    print("=" * 70)
    print(f"通过 {len(PASS)} 项，失败 {len(FAIL)} 项")
    if FAIL:
        for name in FAIL:
            print("  失败：" + name)
    return 1 if FAIL else 0


if __name__ == "__main__":
    sys.exit(main())
