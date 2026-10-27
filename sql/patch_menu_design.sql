-- ⚠️⚠️⚠️ 【已废弃 · 请勿执行】⚠️⚠️⚠️
-- 本脚本已被 sql/fix_menu_full.sql 完全取代，请执行后者。
-- 原因：本脚本基于「SQL 脚本描述的菜单状态」编写，但实际数据库 erp-data 中
--       系统管理 / 销售 / 零售 / 结算 / 报表 整棵树都不存在，且存在 2 个孤儿
--       节点（M000102.parent_id=20、M000106.parent_id=27），另有 role_id=1
--       只绑子菜单不绑目录的问题 —— 这些都不在本脚本覆盖范围内。
-- fix_menu_full.sql 采用「UPSERT + 映射回填 parent_id + 全量授权」三段式，
-- 可重复执行且能自愈上述全部问题，已于 2026-10-01 实测通过（16 → 71 条）。
-- =====================================================================

-- =====================================================================
-- ERP 后台菜单补丁脚本（只新增缺失项，可重复执行）
-- 生成日期：2026-10-01
-- 依据：docs/ERP功能分析与后台菜单设计.md
--
-- ⚠️ 执行前请先备份：
--   CREATE TABLE sys_menu_bak_20261001      AS SELECT * FROM sys_menu;
--   CREATE TABLE sys_role_menu_bak_20261001 AS SELECT * FROM sys_role_menu;
--
-- 说明：
--   menu_type : 0=目录  1=菜单  2=按钮
--   visible   : 1=显示  0=隐藏
--   status    : 1=启用  0=停用
--   本脚本 **不改动** 已有记录，只补缺失；编码冲突（M000105）需人工确认后再处理。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. 查询：先看现状，确认没有编码重复
-- ---------------------------------------------------------------------
SELECT '=== 编码重复检查（应为空）===' AS info;
SELECT code, COUNT(*) AS c FROM sys_menu GROUP BY code HAVING c > 1;

SELECT '=== 当前菜单树 ===' AS info;
SELECT m.id, m.parent_id, p.title AS parent_title, m.code, m.title,
       m.menu_type, m.path, m.permission
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.id
ORDER BY m.parent_id, m.sort;

-- 取 admin 角色
SET @admin_role_id = (SELECT id FROM sys_role
                      WHERE code IN ('admin','ADMIN','R000001') OR name LIKE '%管理员%'
                      LIMIT 1);
SELECT CONCAT('admin 角色ID = ', IFNULL(@admin_role_id,'NULL')) AS info;


-- =====================================================================
-- 1. 补齐：采购收货单（M000302）—— 功能已实现但菜单缺失
-- =====================================================================
SET @purchase_parent_id = (SELECT id FROM sys_menu WHERE code = 'M0003' LIMIT 1);
SET @purchase_parent_id = IFNULL(@purchase_parent_id,
                                (SELECT parent_id FROM sys_menu WHERE code = 'M000301' LIMIT 1));

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @purchase_parent_id, 'M000302', 'purchase-receipt', '采购收货单', 1,
       '/business/purchase/receipt', 'Business/Purchase/Receipt/List',
       'business:purchase:receipt', 'InboxOutlined', 2, 1, 1, '采购收货单管理'
FROM DUAL
WHERE @purchase_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000302');


-- =====================================================================
-- 2. 修复：零售菜单标题乱码（零售管理 / 零售出库单 / 零售退货单）
--    仅修正 title，不动编码与路径
-- =====================================================================
UPDATE sys_menu SET title = '零售管理'   WHERE code = 'M0006'    AND title <> '零售管理';
UPDATE sys_menu SET title = '零售出库单' WHERE code = 'M000602'  AND title <> '零售出库单';
UPDATE sys_menu SET title = '零售退货单' WHERE code = 'M000603'  AND title <> '零售退货单';

UPDATE sys_menu SET title = '零售配置'   WHERE code = 'M000601'  AND title <> '零售配置';


-- =====================================================================
-- 3. 修复：权限串混入中文
-- =====================================================================
UPDATE sys_menu SET permission = 'system:role:assign'
WHERE code = 'M000011' AND permission = 'system:role:授权';


-- =====================================================================
-- 4. 新增：系统管理 → 字典管理 / 操作日志 / 登录日志 / 系统参数 / 编号规则 / 通知公告
--    前两项后端已有实体或接口，只缺菜单
-- =====================================================================
SET @sys_parent_id = (SELECT id FROM sys_menu WHERE code = 'M000001' LIMIT 1);

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @sys_parent_id, 'M000020', 'dict', '字典管理', 1,
       '/system/dict', 'System/Dict', 'system:dict:list', 'BookOutlined', 5, 1, 1, '数据字典管理'
FROM DUAL WHERE @sys_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000020');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @sys_parent_id, 'M000021', 'operation-log', '操作日志', 1,
       '/system/operation-log', 'System/OperationLog', 'system:log:operation:list', 'FileSearchOutlined', 6, 1, 1, '用户操作日志'
FROM DUAL WHERE @sys_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000021');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @sys_parent_id, 'M000022', 'login-log', '登录日志', 1,
       '/system/login-log', 'System/LoginLog', 'system:log:login:list', 'LoginOutlined', 7, 1, 1, '用户登录日志'
FROM DUAL WHERE @sys_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000022');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @sys_parent_id, 'M000023', 'param', '系统参数', 1,
       '/system/param', 'System/Param', 'system:param:list', 'ToolOutlined', 8, 1, 1, '系统参数配置'
FROM DUAL WHERE @sys_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000023');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @sys_parent_id, 'M000024', 'code-rule', '编号规则', 1,
       '/system/code-rule', 'System/CodeRule', 'system:code-rule:list', 'NumberOutlined', 9, 1, 1, '单据编号规则'
FROM DUAL WHERE @sys_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000024');


-- =====================================================================
-- 5. 新增：基础数据 → 支付方式 / 物流公司 / 会员 / 门店 / 地址库 / 打印模板
-- =====================================================================
SET @basedata_parent_id = (SELECT id FROM sys_menu WHERE code = 'M000100' LIMIT 1);

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @basedata_parent_id, 'M000109', 'pay-type', '支付方式', 1,
       '/basedata/pay-type', 'Basedata/PayType', 'basedata:pay-type:list', 'WalletOutlined', 9, 1, 1, '支付方式维护'
FROM DUAL WHERE @basedata_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000109');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @basedata_parent_id, 'M000110', 'logistics-company', '物流公司', 1,
       '/basedata/logistics-company', 'Basedata/LogisticsCompany', 'basedata:logistics:list', 'CarOutlined', 10, 1, 1, '物流公司维护'
FROM DUAL WHERE @basedata_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000110');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @basedata_parent_id, 'M000111', 'member', '会员管理', 1,
       '/basedata/member', 'Basedata/Member', 'basedata:member:list', 'SmileOutlined', 11, 1, 1, '零售会员维护'
FROM DUAL WHERE @basedata_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000111');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @basedata_parent_id, 'M000112', 'shop', '门店管理', 1,
       '/basedata/shop', 'Basedata/Shop', 'basedata:shop:list', 'ShopOutlined', 12, 1, 1, '零售门店维护'
FROM DUAL WHERE @basedata_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000112');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @basedata_parent_id, 'M000113', 'address', '地址库', 1,
       '/basedata/address', 'Basedata/Address', 'basedata:address:list', 'EnvironmentOutlined', 13, 1, 1, '收发货地址库'
FROM DUAL WHERE @basedata_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000113');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @basedata_parent_id, 'M000114', 'print-template', '打印模板', 1,
       '/basedata/print-template', 'Basedata/PrintTemplate', 'basedata:print:list', 'PrinterOutlined', 14, 1, 1, '单据打印模板'
FROM DUAL WHERE @basedata_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000114');


-- =====================================================================
-- 6. 新增：库存管理 → 库存变动记录 / 库存预警
--    父级取「库存管理」目录（优先 M0002，兜底取 库存查询 的父级）
-- =====================================================================
SET @stock_parent_id = (SELECT id FROM sys_menu WHERE code = 'M0002' LIMIT 1);
SET @stock_parent_id = IFNULL(@stock_parent_id,
                             (SELECT parent_id FROM sys_menu WHERE code = 'M000201' LIMIT 1));

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @stock_parent_id, 'M000202', 'stock-log', '库存变动记录', 1,
       '/business/stock/log', 'Business/Stock/Log', 'business:stock:log', 'HistoryOutlined', 2, 1, 1, '库存出入库流水'
FROM DUAL WHERE @stock_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000202');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @stock_parent_id, 'M000504', 'stock-warning', '库存预警', 1,
       '/business/stock/warning', 'Business/Stock/Warning', 'stock:warning:query', 'AlertOutlined', 5, 1, 1, '库存上下限预警'
FROM DUAL WHERE @stock_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000504');


-- =====================================================================
-- 7. 新增：物流管理（一级目录 + 2 个子菜单）
-- =====================================================================
INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT 0, 'M000900', 'logistics', '物流管理', 0,
       '/logistics', NULL, NULL, 'SendOutlined', 90, 1, 1, '物流管理模块'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000900');

SET @logistics_parent_id = (SELECT id FROM sys_menu WHERE code = 'M000900' LIMIT 1);

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @logistics_parent_id, 'M000901', 'logistics-sheet', '物流单', 1,
       '/business/logistics/sheet', 'Business/Logistics/Sheet/List', 'logistics:sheet:query', 'ProfileOutlined', 1, 1, 1, '物流单管理'
FROM DUAL WHERE @logistics_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000901');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @logistics_parent_id, 'M000902', 'logistics-delivery', '批量发货', 1,
       '/business/logistics/delivery', 'Business/Logistics/Delivery/List', 'logistics:delivery:import', 'RocketOutlined', 2, 1, 1, '批量发货'
FROM DUAL WHERE @logistics_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000902');


-- =====================================================================
-- 8. 新增：消息中心（一级目录 + 3 个子菜单）
-- =====================================================================
INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT 0, 'M001100', 'message', '消息中心', 0,
       '/message', NULL, NULL, 'BellOutlined', 91, 1, 1, '消息中心'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M001100');

SET @message_parent_id = (SELECT id FROM sys_menu WHERE code = 'M001100' LIMIT 1);

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @message_parent_id, 'M001101', 'notice', '通知公告', 1,
       '/message/notice', 'Message/Notice/List', 'message:notice:list', 'NotificationOutlined', 1, 1, 1, '通知公告'
FROM DUAL WHERE @message_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M001101');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @message_parent_id, 'M001102', 'site-message', '站内信', 1,
       '/message/site', 'Message/Site/List', 'message:site:list', 'MailOutlined', 2, 1, 1, '站内信'
FROM DUAL WHERE @message_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M001102');

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @message_parent_id, 'M001103', 'notify-group', '消息通知组', 1,
       '/message/group', 'Message/Group/List', 'message:group:list', 'TeamOutlined', 3, 1, 1, '消息通知组'
FROM DUAL WHERE @message_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M001103');


-- =====================================================================
-- 9. 新增：报表 → 往来对账汇总
-- =====================================================================
SET @chart_parent_id = (SELECT id FROM sys_menu WHERE code = 'M000800' LIMIT 1);

INSERT INTO `sys_menu`
  (`parent_id`,`code`,`name`,`title`,`menu_type`,`path`,`component`,`permission`,`icon`,`sort`,`visible`,`status`,`remark`)
SELECT @chart_parent_id, 'M000806', 'chart-settle', '往来对账汇总', 1,
       '/chart/settle', '/Business/Chart/Settle/index', 'chart:settle', 'AccountBookOutlined', 6, 1, 1, '应收应付往来汇总'
FROM DUAL WHERE @chart_parent_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE code = 'M000806');


-- =====================================================================
-- 10. 给 admin 角色授权本次新增的全部菜单（含按钮）
-- =====================================================================
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT @admin_role_id, m.id
FROM sys_menu m
WHERE m.code IN (
        'M000302','M000020','M000021','M000022','M000023','M000024',
        'M000109','M000110','M000111','M000112','M000113','M000114',
        'M000202','M000504',
        'M000900','M000901','M000902',
        'M001100','M001101','M001102','M001103',
        'M000806'
      )
  AND @admin_role_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = @admin_role_id AND rm.menu_id = m.id);

-- 同时把本次新增菜单挂到各自父级（若父级尚未授权）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT @admin_role_id, m.parent_id
FROM sys_menu m
WHERE m.code IN ('M000302','M000020','M000109','M000202','M000901','M001101','M000806')
  AND m.parent_id > 0
  AND @admin_role_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = @admin_role_id AND rm.menu_id = m.parent_id);


-- =====================================================================
-- 11. 验证：输出本次新增的菜单
-- =====================================================================
SELECT '=== 新增/修复后的菜单树（本次涉及部分）===' AS info;
SELECT m.id, m.parent_id, p.title AS parent_title, m.code, m.title,
       m.menu_type, m.path, m.component, m.permission, m.sort
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.id
WHERE m.code IN (
        'M000302','M000020','M000021','M000022','M000023','M000024',
        'M000109','M000110','M000111','M000112','M000113','M000114',
        'M000202','M000504',
        'M000900','M000901','M000902',
        'M001100','M001101','M001102','M001103',
        'M000806','M0006','M000601','M000602','M000603'
      )
ORDER BY m.parent_id, m.sort;

SELECT '=== 编码重复复查（应为空）===' AS info;
SELECT code, COUNT(*) AS c FROM sys_menu GROUP BY code HAVING c > 1;
