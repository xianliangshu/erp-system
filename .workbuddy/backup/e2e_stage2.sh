#!/usr/bin/env bash
# 库存体系补齐 · 第二段端到端实测（盘点任务 + 预先盘点单）
#
# 覆盖：
#   0  未授权访问 → 401
#   1  登录
#   2  盘点任务创建（全场，写库存快照）
#   3  快照隔离（改库存不影响快照）
#   4  录入盘点数量
#   5  生成差异 → 落成盘点单并回填 plan_id
#   6  状态约束（非「任务生成」不可录入/作废）
#   7  作废
#   8  盘点范围兜底
#   9  预先盘点单 CRUD（三轮数量独立）
#  10  跨模块下拉免权限
#  11  盘点单手工关联来源（preSheetId）
#  12  @Scheduled 超时自动作废
#
# ⚠️ 会写库：脚本头部重置 product_stock 并清理 E2E-* 测试数据。
set -u
BASE=http://127.0.0.1:8082/api
HERE="$(cd "$(dirname "$0")" && pwd)"
MYSQL=/d/soft/mysql-8.4.10-winx64/bin/mysql.exe
PY=/c/Users/shu/.workbuddy/binaries/python/versions/3.13.12/python.exe
PASS=0; FAIL=0

jqget() { "$PY" "$HERE/jq.py" "$1" 2>/dev/null; }

# 数值兜底：BigDecimal 会序列化成 90.0，与期望的 90 做等价比较
norm() { echo "$1" | sed 's/\.0*$//'; }

ok()   { PASS=$((PASS+1)); echo "  [PASS] $1"; }
bad()  { FAIL=$((FAIL+1)); echo "  [FAIL] $1"; }
check() {
    local label="$1" actual expect
    actual="$(norm "$2")"; expect="$(norm "$3")"
    if [ "$actual" = "$expect" ]; then ok "$label ($actual)"
    else bad "$label 期望=$expect 实际=$actual"; fi
}

sql() { "$MYSQL" -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 erp-data -N -B -e "$1" 2>/dev/null; }

echo "==================== 前置：重置基线数据 ===================="
sql "
DELETE d FROM stock_check_detail d JOIN stock_check c ON c.id = d.check_id
  WHERE c.description LIKE 'E2E%' OR c.plan_id IS NOT NULL;
DELETE FROM stock_check WHERE description LIKE 'E2E%' OR plan_id IS NOT NULL;
DELETE d FROM take_stock_plan_detail d JOIN take_stock_plan p ON p.id = d.plan_id
  WHERE p.description LIKE 'E2E%';
DELETE FROM take_stock_plan WHERE description LIKE 'E2E%';
DELETE d FROM pre_take_stock_sheet_detail d JOIN pre_take_stock_sheet s ON s.id = d.sheet_id
  WHERE s.description LIKE 'E2E%';
DELETE FROM pre_take_stock_sheet WHERE description LIKE 'E2E%';
UPDATE product_stock SET stock_num = 90 WHERE sc_id = 2 AND product_id = 1;
"
echo "  product_stock(sc2,p1).stock_num = $(sql 'SELECT stock_num FROM product_stock WHERE sc_id=2 AND product_id=1')"

echo "==================== 0. 未授权访问 ===================="
check "无 token 访问盘点任务分页"   "$(curl -s -o /dev/null -w '%{http_code}' -m 5 "$BASE/stock/take/plan/page")" "401"
check "无 token 访问预先盘点单分页" "$(curl -s -o /dev/null -w '%{http_code}' -m 5 "$BASE/stock/take/pre/page")" "401"

echo "==================== 1. 登录 ===================="
LOGIN=$(curl -s -m 10 -X POST "$BASE/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}')
TOKEN=$(echo "$LOGIN" | jqget "d['data']['token']")
if [ -n "$TOKEN" ]; then ok "登录成功 token=${TOKEN:0:8}..."; else bad "登录失败: $LOGIN"; exit 1; fi
H="Authorization: Bearer $TOKEN"
CT="Content-Type: application/json"

echo "==================== 2. 盘点任务：创建（全场，写快照） ===================="
R=$(curl -s -m 10 -X POST "$BASE/stock/take/plan" -H "$H" -H "$CT" \
     -d '{"scId":2,"takeType":0,"description":"E2E-全场盘点"}')
PLAN=$(echo "$R" | jqget "d['data']")
if [ -n "$PLAN" ] && [ "$PLAN" != "None" ]; then ok "创建盘点任务 id=$PLAN"; else bad "创建失败: $R"; exit 1; fi

R=$(curl -s -m 10 "$BASE/stock/take/plan/$PLAN" -H "$H")
check "任务状态=任务生成(0)"        "$(echo "$R" | jqget "d['data']['takeStatus']")" "0"
check "明细总数=1（该仓库1个商品）" "$(echo "$R" | jqget "d['data']['detailCount']")" "1"
check "已录入数=0"                 "$(echo "$R" | jqget "d['data']['enteredCount']")" "0"
check "快照库存=90"                "$(echo "$R" | jqget "d['data']['details'][0]['stockNum']")" "90"
check "当前库存=90"                "$(echo "$R" | jqget "d['data']['details'][0]['currentStockNum']")" "90"
check "商品名称已回填"              "$(echo "$R" | jqget "d['data']['details'][0]['productName'] is not None")" "True"
check "任务编号 PD 前缀"            "$(echo "$R" | jqget "str(d['data']['code']).startswith('PD')")" "True"
PLAN_DET=$(echo "$R" | jqget "d['data']['details'][0]['id']")

echo "==================== 3. 快照隔离：改库存不影响快照 ===================="
sql "UPDATE product_stock SET stock_num = 95 WHERE sc_id = 2 AND product_id = 1;"
R=$(curl -s -m 10 "$BASE/stock/take/plan/$PLAN" -H "$H")
check "库存改动后快照仍=90"   "$(echo "$R" | jqget "d['data']['details'][0]['stockNum']")" "90"
check "库存改动后当前库存=95" "$(echo "$R" | jqget "d['data']['details'][0]['currentStockNum']")" "95"

echo "==================== 4. 录入盘点数量 ===================="
R=$(curl -s -m 10 -X PUT "$BASE/stock/take/plan/take-nums" -H "$H" -H "$CT" \
     -d "{\"planId\":$PLAN,\"details\":[{\"id\":$PLAN_DET,\"takeNum\":92,\"description\":\"E2E实盘\"}]}")
check "录入盘点数量 code=200" "$(echo "$R" | jqget "d['code']")" "200"
R=$(curl -s -m 10 "$BASE/stock/take/plan/$PLAN" -H "$H")
check "盘点数量已保存=92"     "$(echo "$R" | jqget "d['data']['details'][0]['takeNum']")" "92"
check "差异=+2（对比快照90）" "$(echo "$R" | jqget "d['data']['details'][0]['diffNum']")" "2"
check "已录入数=1"            "$(echo "$R" | jqget "d['data']['enteredCount']")" "1"

echo "==================== 5. 生成差异 → 落成盘点单 ===================="
R=$(curl -s -m 15 -X POST "$BASE/stock/take/plan/generate-diff/$PLAN" -H "$H")
CHECK=$(echo "$R" | jqget "d['data']")
if [ -n "$CHECK" ] && [ "$CHECK" != "None" ]; then ok "生成盘点单 id=$CHECK"; else bad "生成差异失败: $R"; CHECK=""; fi
R=$(curl -s -m 10 "$BASE/stock/take/plan/$PLAN" -H "$H")
check "任务状态=差异已生成(6)" "$(echo "$R" | jqget "d['data']['takeStatus']")" "6"
check "回填 checkId"          "$(echo "$R" | jqget "d['data']['checkId']")" "$CHECK"
if [ -n "$CHECK" ]; then
    R=$(curl -s -m 10 "$BASE/business/stock/check/$CHECK" -H "$H")
    check "盘点单来源 planId 已写入"       "$(echo "$R" | jqget "d['data']['stockCheck']['planId']")" "$PLAN"
    check "盘点单账面数=快照90（非当前95）" "$(echo "$R" | jqget "d['data']['details'][0]['stockNum']")" "90"
    check "盘点单实盘数=92"               "$(echo "$R" | jqget "d['data']['details'][0]['actualNum']")" "92"
    check "盘点单差异=+2"                 "$(echo "$R" | jqget "d['data']['details'][0]['diffNum']")" "2"
    check "盘点单待审核(0)"               "$(echo "$R" | jqget "d['data']['stockCheck']['status']")" "0"
    check "生成后库存未被自动改动（autoChangeStock=0）" "$(sql 'SELECT stock_num FROM product_stock WHERE sc_id=2 AND product_id=1')" "95"
fi

echo "==================== 6. 状态约束（非「任务生成」不可录入/作废） ===================="
R=$(curl -s -m 10 -X PUT "$BASE/stock/take/plan/take-nums" -H "$H" -H "$CT" \
     -d "{\"planId\":$PLAN,\"details\":[{\"id\":$PLAN_DET,\"takeNum\":1}]}")
MSG=$(echo "$R" | jqget "d['message']")
if echo "$MSG" | grep -q "任务生成"; then ok "已生成差异的任务拒绝再录入：$MSG"; else bad "状态约束未生效: $R"; fi
R=$(curl -s -m 10 -X POST "$BASE/stock/take/plan/cancel/$PLAN" -H "$H")
MSG=$(echo "$R" | jqget "d['message']")
if echo "$MSG" | grep -q "任务生成"; then ok "已生成差异的任务拒绝作废：$MSG"; else bad "作废约束未生效: $R"; fi

echo "==================== 7. 作废（新建任务走正常作废） ===================="
R=$(curl -s -m 10 -X POST "$BASE/stock/take/plan" -H "$H" -H "$CT" \
     -d '{"scId":2,"takeType":1,"bizId":"1","description":"E2E-单品盘点"}')
PLAN2=$(echo "$R" | jqget "d['data']")
if [ -n "$PLAN2" ] && [ "$PLAN2" != "None" ]; then ok "创建单品任务 id=$PLAN2"; else bad "创建单品任务失败: $R"; fi
check "作废返回成功" "$(curl -s -m 10 -X POST "$BASE/stock/take/plan/cancel/$PLAN2" -H "$H" | jqget "d['code']")" "200"
R=$(curl -s -m 10 "$BASE/stock/take/plan/$PLAN2" -H "$H")
check "状态=已作废(12)" "$(echo "$R" | jqget "d['data']['takeStatus']")" "12"
check "作废后不可再录入" "$(curl -s -m 10 -X PUT "$BASE/stock/take/plan/take-nums" -H "$H" -H "$CT" -d "{\"planId\":$PLAN2,\"details\":[{\"id\":1,\"takeNum\":1}]}" | jqget "str(d['message']).find('任务生成')>=0")" "True"

echo "==================== 8. 盘点范围兜底 ===================="
R=$(curl -s -m 10 -X POST "$BASE/stock/take/plan" -H "$H" -H "$CT" \
     -d '{"scId":2,"takeType":2,"bizId":"999999","description":"E2E-不存在的分类"}')
MSG=$(echo "$R" | jqget "d['message']")
if echo "$MSG" | grep -q "没有可盘点"; then ok "空范围被拒绝：$MSG"; else bad "空范围未拦截: $R"; fi
R=$(curl -s -m 10 -X POST "$BASE/stock/take/plan" -H "$H" -H "$CT" -d '{"scId":2,"takeType":1}')
MSG=$(echo "$R" | jqget "d['message']")
if echo "$MSG" | grep -q "请选择要盘点的商品"; then ok "单品未选范围被拒绝：$MSG"; else bad "未拦截: $R"; fi
R=$(curl -s -m 10 -X POST "$BASE/stock/take/plan" -H "$H" -H "$CT" -d '{"takeType":0}')
MSG=$(echo "$R" | jqget "d['message']")
if echo "$MSG" | grep -q "请选择仓库"; then ok "未选仓库被拒绝：$MSG"; else bad "未拦截: $R"; fi

echo "==================== 9. 预先盘点单 CRUD ===================="
R=$(curl -s -m 10 -X POST "$BASE/stock/take/pre" -H "$H" -H "$CT" \
     -d '{"scId":2,"takeStatus":0,"description":"E2E-初盘","details":[{"productId":1,"firstNum":88,"secondNum":null,"randNum":null}]}')
PRE=$(echo "$R" | jqget "d['data']")
if [ -n "$PRE" ] && [ "$PRE" != "None" ]; then ok "创建预先盘点单 id=$PRE"; else bad "创建失败: $R"; fi
R=$(curl -s -m 10 "$BASE/stock/take/pre/$PRE" -H "$H")
check "轮次=初盘(0)"             "$(echo "$R" | jqget "d['data']['takeStatus']")" "0"
check "初盘数量=88"              "$(echo "$R" | jqget "d['data']['details'][0]['firstNum']")" "88"
check "复盘数量为空（三轮独立）"  "$(echo "$R" | jqget "d['data']['details'][0]['secondNum']")" "None"
check "商品名称已回填"           "$(echo "$R" | jqget "d['data']['details'][0]['productName'] is not None")" "True"
check "单据编号 YPD 前缀"        "$(echo "$R" | jqget "str(d['data']['code']).startswith('YPD')")" "True"
PRE_DET=$(echo "$R" | jqget "d['data']['details'][0]['id']")

R=$(curl -s -m 10 -X PUT "$BASE/stock/take/pre" -H "$H" -H "$CT" \
     -d "{\"id\":$PRE,\"scId\":2,\"takeStatus\":1,\"description\":\"E2E-复盘\",\"details\":[{\"id\":$PRE_DET,\"productId\":1,\"firstNum\":88,\"secondNum\":88,\"randNum\":null}]}")
check "修改返回 code=200" "$(echo "$R" | jqget "d['code']")" "200"
R=$(curl -s -m 10 "$BASE/stock/take/pre/$PRE" -H "$H")
check "修改后轮次=复盘(1)"  "$(echo "$R" | jqget "d['data']['takeStatus']")" "1"
check "修改后复盘数量=88"   "$(echo "$R" | jqget "d['data']['details'][0]['secondNum']")" "88"
check "修改后初盘数量保留"  "$(echo "$R" | jqget "d['data']['details'][0]['firstNum']")" "88"
check "修改后仍只有1条明细" "$(echo "$R" | jqget "len(d['data']['details'])")" "1"

R=$(curl -s -m 10 -X POST "$BASE/stock/take/pre" -H "$H" -H "$CT" \
     -d '{"scId":2,"takeStatus":0,"details":[{"productId":1,"firstNum":1},{"productId":1,"firstNum":2}]}')
MSG=$(echo "$R" | jqget "d['message']")
if echo "$MSG" | grep -q "重复"; then ok "重复商品被拒绝：$MSG"; else bad "重复未拦截: $R"; fi
R=$(curl -s -m 10 -X POST "$BASE/stock/take/pre" -H "$H" -H "$CT" -d '{"scId":2,"takeStatus":0,"details":[]}')
MSG=$(echo "$R" | jqget "d['message']")
if echo "$MSG" | grep -q "明细"; then ok "空明细被拒绝：$MSG"; else bad "空明细未拦截: $R"; fi

echo "==================== 10. 下拉接口（跨模块免权限） ===================="
check "任务下拉可访问"       "$(curl -s -m 10 "$BASE/stock/take/plan/options" -H "$H" | jqget "d['code']")" "200"
R=$(curl -s -m 10 "$BASE/stock/take/pre/options" -H "$H")
check "预先盘点单下拉可访问"  "$(echo "$R" | jqget "d['code']")" "200"
check "下拉含刚才的单据"      "$(echo "$R" | jqget "any(int(s['id'])==$PRE for s in d['data'])")" "True"
check "下拉无 token = 401"    "$(curl -s -o /dev/null -w '%{http_code}' -m 5 "$BASE/stock/take/pre/options")" "401"

echo "==================== 11. 盘点单手工关联来源 ===================="
R=$(curl -s -m 10 -X POST "$BASE/business/stock/check" -H "$H" -H "$CT" \
     -d "{\"scId\":2,\"checkDate\":\"2026-10-03\",\"description\":\"E2E-手工单关联来源\",\"preSheetId\":$PRE,\"details\":[{\"productId\":1,\"stockNum\":95,\"actualNum\":95,\"costPrice\":110}]}")
CHECK2=$(echo "$R" | jqget "d['data']")
if [ -n "$CHECK2" ] && [ "$CHECK2" != "None" ]; then ok "创建盘点单 id=$CHECK2"; else bad "创建失败: $R"; fi
R=$(curl -s -m 10 "$BASE/business/stock/check/$CHECK2" -H "$H")
check "手工单 preSheetId 已写入" "$(echo "$R" | jqget "d['data']['stockCheck']['preSheetId']")" "$PRE"
check "手工单 planId 为空"       "$(echo "$R" | jqget "d['data']['stockCheck']['planId']")" "None"

echo "==================== 12. @Scheduled 超时自动作废 ===================="
R=$(curl -s -m 10 -X POST "$BASE/stock/take/plan" -H "$H" -H "$CT" \
     -d '{"scId":2,"takeType":1,"bizId":"1","description":"E2E-超时作废"}')
PLAN3=$(echo "$R" | jqget "d['data']")
if [ -n "$PLAN3" ] && [ "$PLAN3" != "None" ]; then ok "创建待超时任务 id=$PLAN3"; else bad "创建失败: $R"; fi
# 把创建时间回拨 30 小时（cancel_hours=24），等下一个 10 分钟整点触发定时任务
sql "UPDATE take_stock_plan SET create_time = DATE_SUB(NOW(), INTERVAL 30 HOUR) WHERE id = $PLAN3;"
NOW_S=$(date +%s); NEXT=$(( (NOW_S / 600 + 1) * 600 )); WAIT=$(( NEXT - NOW_S + 25 ))
echo "  当前 $(date +%H:%M:%S)，定时任务在每 10 分整触发，等待 ${WAIT}s ..."
sleep "$WAIT"
R=$(curl -s -m 10 "$BASE/stock/take/plan/$PLAN3" -H "$H")
check "超时任务已被自动作废(12)" "$(echo "$R" | jqget "d['data']['takeStatus']")" "12"
check "DB 侧同样为 12"          "$(sql "SELECT take_status FROM take_stock_plan WHERE id=$PLAN3")" "12"

echo
echo "==================== 汇总 ===================="
echo "PASS=$PASS  FAIL=$FAIL"
echo "PLAN=$PLAN PLAN2=$PLAN2 PLAN3=$PLAN3 PRE=$PRE CHECK=$CHECK CHECK2=$CHECK2" > /tmp/e2e_ids.txt
cat /tmp/e2e_ids.txt
