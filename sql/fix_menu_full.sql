-- =====================================================================
-- ERP 菜单数据修复脚本（幂等，可重复执行）
-- 目标库：erp-data
-- 生成日期：2026-10-01
-- 状态：已于 2026-10-01 实测通过（sys_menu 16 → 71 条，0 孤儿 / 0 重复编码）
--
-- ★ 这是**唯一权威**的菜单初始化脚本。
--   以下历史脚本已被本脚本取代，请勿再执行（它们互相独立、无执行顺序约定，
--   且部分内容与实际数据库状态不符）：
--     menu_warehouse.sql / material_menu.sql / add_inventory_menu.sql /
--     add_purchase_menu.sql / add_purchase_return_menu.sql / add_supplier_menu*.sql /
--     add_customer_brand_menus.sql / assign_warehouse_menu_to_admin.sql /
--     sale_order.sql / sale_delivery.sql / sale_return.sql / stock_check.sql /
--     stock_transfer.sql / stock_adjust.sql / retail_module*.sql /
--     settle_module.sql / chart_module.sql / patch_menu_design.sql（先前版本）
--   保留但仅用于建表的：product_stock_log.sql / user-management-database-design.sql
--
--   注：retail_module_utf8.sql 的菜单标题为 GBK/UTF-8 双重编码乱码，
--       且「零售退货单」一处单引号未闭合，直接执行会语法报错 —— 已被本脚本取代。
--
-- 修复内容：
--   1. 修复 2 个孤儿节点：M000102(物料管理) parent 20→基础数据、
--      M000106(供应商管理) parent 27→基础数据
--   2. 规范化一级目录编码：M0002→M000200、M0003→M000300
--      （与 SysMenuServiceImpl.generateMenuCode() 的 M%06d 约定对齐）
--   3. 补齐缺失菜单：系统管理(含用户/角色/部门/菜单/字典)、
--      基础数据(客户/品牌)、库存(流水/调整/调整原因/盘点/调拨)、
--      销售、零售、结算(9项)、统计报表(5项)
--   4. 修正权限串：system:role:授权 → system:role:assign
--   5. 补齐 admin(role_id=1) 的授权：给全部菜单（含目录与按钮）授权，
--      原先只绑定了子菜单、没绑目录，导致菜单树断裂
--
-- 执行方式：
--   mysql -h192.168.31.199 -uroot -p123456 --default-character-set=utf8mb4 \
--         erp-data < sql/fix_menu_full.sql
--
-- 备份（执行前务必先做）：
--   .workbuddy/backup/sys_menu_before_20261001.sql  已包含 sys_menu + sys_role_menu 全量数据
-- =====================================================================

SET NAMES utf8mb4;

-- =====================================================================
-- 0. 执行前快照
-- =====================================================================
SELECT '=== 修复前：菜单总数 ===' AS info;
SELECT COUNT(*) AS menu_total FROM sys_menu WHERE is_deleted = 0;

SELECT '=== 修复前：孤儿节点（parent_id 指向不存在的菜单）===' AS info;
SELECT m.id, m.code, m.title, m.parent_id
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.id
WHERE m.parent_id > 0 AND p.id IS NULL AND m.is_deleted = 0;


-- =====================================================================
-- 1. 规范化一级目录编码（必须在 UPSERT 之前，避免产生重复目录）
-- =====================================================================
UPDATE sys_menu SET code = 'M000200' WHERE code = 'M0002';
UPDATE sys_menu SET code = 'M000300' WHERE code = 'M0003';


-- =====================================================================
-- 2. UPSERT 全部菜单（按 uk_code 唯一键匹配，存在则更新、不存在则插入）
--    字段约定：name = 英文键，title = 中文显示名
-- =====================================================================

-- ---------- 一级目录 ----------
INSERT INTO sys_menu (parent_id, code, name, title, menu_type, path, component, permission, icon, sort, visible, status, remark) VALUES
(0, 'M000001', 'system',   '系统管理', 0, '/system',    NULL, NULL, 'SettingOutlined',        1, 1, 1, '系统管理模块'),
(0, 'M000100', 'basedata', '基础数据', 0, '/basedata',  NULL, NULL, 'DatabaseOutlined',       2, 1, 1, '基础数据管理模块'),
(0, 'M000200', 'inventory','库存管理', 0, '/inventory', NULL, NULL, 'StockOutlined',          3, 1, 1, '库存管理模块'),
(0, 'M000300', 'purchase', '采购管理', 0, '/purchase',  NULL, NULL, 'ShoppingCartOutlined',   4, 1, 1, '采购管理模块'),
(0, 'M000400', 'sale',     '销售管理', 0, '/sale',      NULL, NULL, 'ShoppingOutlined',       5, 1, 1, '销售管理模块'),
(0, 'M000600', 'retail',   '零售管理', 0, '/retail',    NULL, NULL, 'ShopOutlined',           6, 1, 1, '零售管理模块'),
(0, 'M000700', 'settle',   '结算管理', 0, '/settle',    NULL, NULL, 'DollarOutlined',         7, 1, 1, '结算管理模块'),
(0, 'M000800', 'chart',    '统计报表', 0, '/chart',     NULL, NULL, 'AreaChartOutlined',      8, 1, 1, '统计报表模块')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), title=VALUES(title), menu_type=VALUES(menu_type), path=VALUES(path),
  component=VALUES(component), permission=VALUES(permission), icon=VALUES(icon),
  sort=VALUES(sort), visible=VALUES(visible), status=VALUES(status), remark=VALUES(remark), is_deleted=0;

-- ---------- 系统管理 ----------
INSERT INTO sys_menu (parent_id, code, name, title, menu_type, path, component, permission, icon, sort, visible, status, remark) VALUES
(0, 'M000002', 'user',        '用户管理', 1, '/system/user', 'System/User', 'system:user:list', 'UserOutlined',      1, 1, 1, '用户信息管理'),
(0, 'M000003', 'user-add',    '新增用户', 2, NULL, NULL, 'system:user:add',      NULL, 1, 1, 1, '新增用户按钮'),
(0, 'M000004', 'user-edit',   '修改用户', 2, NULL, NULL, 'system:user:edit',     NULL, 2, 1, 1, '修改用户按钮'),
(0, 'M000005', 'user-delete', '删除用户', 2, NULL, NULL, 'system:user:delete',   NULL, 3, 1, 1, '删除用户按钮'),
(0, 'M000006', 'user-reset',  '重置密码', 2, NULL, NULL, 'system:user:resetPwd', NULL, 4, 1, 1, '重置密码按钮'),
(0, 'M000007', 'role',        '角色管理', 1, '/system/role', 'System/Role', 'system:role:list', 'TeamOutlined',      2, 1, 1, '角色与授权管理'),
(0, 'M000008', 'role-add',    '新增角色', 2, NULL, NULL, 'system:role:add',    NULL, 1, 1, 1, '新增角色按钮'),
(0, 'M000009', 'role-edit',   '修改角色', 2, NULL, NULL, 'system:role:edit',   NULL, 2, 1, 1, '修改角色按钮'),
(0, 'M000010', 'role-delete', '删除角色', 2, NULL, NULL, 'system:role:delete', NULL, 3, 1, 1, '删除角色按钮'),
(0, 'M000011', 'role-assign', '分配权限', 2, NULL, NULL, 'system:role:assign', NULL, 4, 1, 1, '角色分配菜单权限'),
(0, 'M000012', 'dept',        '部门管理', 1, '/system/dept', 'System/Dept', 'system:dept:list', 'ApartmentOutlined', 3, 1, 1, '组织部门管理'),
(0, 'M000013', 'dept-add',    '新增部门', 2, NULL, NULL, 'system:dept:add',    NULL, 1, 1, 1, '新增部门按钮'),
(0, 'M000014', 'dept-edit',   '修改部门', 2, NULL, NULL, 'system:dept:edit',   NULL, 2, 1, 1, '修改部门按钮'),
(0, 'M000015', 'dept-delete', '删除部门', 2, NULL, NULL, 'system:dept:delete', NULL, 3, 1, 1, '删除部门按钮'),
(0, 'M000016', 'menu',        '菜单管理', 1, '/system/menu', 'System/Menu', 'system:menu:list', 'MenuOutlined',      4, 1, 1, '菜单与权限项管理'),
(0, 'M000017', 'menu-add',    '新增菜单', 2, NULL, NULL, 'system:menu:add',    NULL, 1, 1, 1, '新增菜单按钮'),
(0, 'M000018', 'menu-edit',   '修改菜单', 2, NULL, NULL, 'system:menu:edit',   NULL, 2, 1, 1, '修改菜单按钮'),
(0, 'M000019', 'menu-delete', '删除菜单', 2, NULL, NULL, 'system:menu:delete', NULL, 3, 1, 1, '删除菜单按钮'),
(0, 'M000020', 'dict',        '字典管理', 1, '/system/dict', 'System/Dict', 'system:dict:list', 'BookOutlined',      5, 1, 1, '数据字典管理'),
(0, 'M000021', 'dict-add',    '新增字典', 2, NULL, NULL, 'system:dict:add',    NULL, 1, 1, 1, '新增字典按钮'),
(0, 'M000022', 'dict-edit',   '修改字典', 2, NULL, NULL, 'system:dict:edit',   NULL, 2, 1, 1, '修改字典按钮'),
(0, 'M000023', 'dict-delete', '删除字典', 2, NULL, NULL, 'system:dict:delete', NULL, 3, 1, 1, '删除字典按钮')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), title=VALUES(title), menu_type=VALUES(menu_type), path=VALUES(path),
  component=VALUES(component), permission=VALUES(permission), icon=VALUES(icon),
  sort=VALUES(sort), visible=VALUES(visible), status=VALUES(status), remark=VALUES(remark), is_deleted=0;

-- ---------- 基础数据 ----------
INSERT INTO sys_menu (parent_id, code, name, title, menu_type, path, component, permission, icon, sort, visible, status, remark) VALUES
(0, 'M000101', 'warehouse',       '仓库管理', 1, '/basedata/warehouse',       'Basedata/Warehouse',            'basedata:warehouse:list',     'HomeOutlined', 1, 1, 1, '仓库信息管理'),
(0, 'M000102', 'material',        '物料管理', 0, '/basedata/material',        NULL,                            NULL,                          'AppstoreOutlined', 2, 1, 1, '物料管理模块'),
(0, 'M000103', 'material-category','物料分类',1, '/basedata/material/category','Basedata/Material/Category',    'basedata:material:category',  'FolderOutlined', 1, 1, 1, '物料分类管理'),
(0, 'M000104', 'unit',            '计量单位', 1, '/basedata/material/unit',   'Basedata/Material/Unit',        'basedata:material:unit',      'CalculatorOutlined', 2, 1, 1, '计量单位管理'),
(0, 'M000105', 'material-info',   '物料信息', 1, '/basedata/material/info',   'Basedata/Material/Info',        'basedata:material:info',      'InboxOutlined', 3, 1, 1, '物料信息管理'),
(0, 'M000106', 'supplier',        '供应商管理',1,'/basedata/supplier',         'Basedata/Supplier',             'basedata:supplier:list',      'TeamOutlined', 3, 1, 1, '供应商信息管理'),
(0, 'M000107', 'customer',        '客户管理', 1, '/basedata/customer',        'Basedata/Customer',             'basedata:customer:list',      'UserOutlined', 4, 1, 1, '客户信息管理'),
(0, 'M000108', 'brand',           '品牌管理', 1, '/basedata/brand',           'Basedata/Brand',                'basedata:brand:list',         'TagOutlined', 5, 1, 1, '品牌信息管理')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), title=VALUES(title), menu_type=VALUES(menu_type), path=VALUES(path),
  component=VALUES(component), permission=VALUES(permission), icon=VALUES(icon),
  sort=VALUES(sort), visible=VALUES(visible), status=VALUES(status), remark=VALUES(remark), is_deleted=0;

-- ---------- 库存管理 ----------
INSERT INTO sys_menu (parent_id, code, name, title, menu_type, path, component, permission, icon, sort, visible, status, remark) VALUES
(0, 'M000201', 'stock-query',        '库存查询', 1, '/business/stock/query',   'Business/Stock/Query',              'business:stock:query',        'SearchOutlined', 1, 1, 1, '实时库存查询'),
(0, 'M000202', 'stock-log',          '库存流水', 1, '/business/stock/log',     'Business/Stock/Log',                'business:stock:log',          'HistoryOutlined', 2, 1, 1, '库存出入库变动记录'),
(0, 'M000203', 'stock-adjust-sheet', '库存调整', 1, '/stock/adjust/sheet',     'Business/Stock/Adjust/Sheet/List',  'stock:adjust:query',          'SwapOutlined', 3, 1, 1, '库存调整单管理'),
(0, 'M000204', 'stock-adjust-reason','调整原因', 1, '/stock/adjust/reason',    'Business/Stock/Adjust/Reason',      'stock:adjust:reason:query',   'SettingOutlined', 4, 1, 1, '库存调整原因维护'),
(0, 'M000205', 'stock-check',        '库存盘点', 1, '/business/stock/check',   'Business/Stock/Check/List',         'business:stock:check',        'AuditOutlined', 5, 1, 1, '库存盘点管理'),
(0, 'M000206', 'stock-transfer',     '库存调拨', 1, '/business/stock/transfer','Business/Stock/Transfer/List',      'business:stock:transfer',     'NodeIndexOutlined', 6, 1, 1, '仓库间调拨管理')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), title=VALUES(title), menu_type=VALUES(menu_type), path=VALUES(path),
  component=VALUES(component), permission=VALUES(permission), icon=VALUES(icon),
  sort=VALUES(sort), visible=VALUES(visible), status=VALUES(status), remark=VALUES(remark), is_deleted=0;

-- ---------- 采购管理（补 M000302 采购收货）----------
INSERT INTO sys_menu (parent_id, code, name, title, menu_type, path, component, permission, icon, sort, visible, status, remark) VALUES
(0, 'M000301', 'purchase-order',   '采购订单', 1, '/business/purchase/order',   'Business/Purchase/Order/List',   'business:purchase:order',   'FileTextOutlined', 1, 1, 1, '采购订单管理'),
(0, 'M000302', 'purchase-receipt', '采购收货', 1, '/business/purchase/receipt', 'Business/Purchase/Receipt/List', 'business:purchase:receipt', 'InboxOutlined', 2, 1, 1, '采购收货单管理'),
(0, 'M000303', 'purchase-return',  '采购退货', 1, '/business/purchase/return',  'Business/Purchase/Return/List',  'business:purchase:return',  'RollbackOutlined', 3, 1, 1, '采购退货管理')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), title=VALUES(title), menu_type=VALUES(menu_type), path=VALUES(path),
  component=VALUES(component), permission=VALUES(permission), icon=VALUES(icon),
  sort=VALUES(sort), visible=VALUES(visible), status=VALUES(status), remark=VALUES(remark), is_deleted=0;

-- ---------- 销售管理 ----------
INSERT INTO sys_menu (parent_id, code, name, title, menu_type, path, component, permission, icon, sort, visible, status, remark) VALUES
(0, 'M000401', 'sale-order',    '销售订单', 1, '/business/sale/order',    'Business/Sale/Order/List',    'business:sale:order',    'FileTextOutlined', 1, 1, 1, '销售订单管理'),
(0, 'M000402', 'sale-delivery', '销售出库', 1, '/business/sale/delivery', 'Business/Sale/Delivery/List', 'business:sale:delivery', 'ExportOutlined',   2, 1, 1, '销售出库单管理'),
(0, 'M000403', 'sale-return',   '销售退货', 1, '/business/sale/return',   'Business/Sale/Return/List',   'business:sale:return',   'ImportOutlined',   3, 1, 1, '销售退货单管理')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), title=VALUES(title), menu_type=VALUES(menu_type), path=VALUES(path),
  component=VALUES(component), permission=VALUES(permission), icon=VALUES(icon),
  sort=VALUES(sort), visible=VALUES(visible), status=VALUES(status), remark=VALUES(remark), is_deleted=0;

-- ---------- 零售管理 ----------
INSERT INTO sys_menu (parent_id, code, name, title, menu_type, path, component, permission, icon, sort, visible, status, remark) VALUES
(0, 'M000601', 'retail-config', '零售配置', 1, '/business/retail/config', 'Business/Retail/Config',          'business:retail:config', 'SettingOutlined', 1, 1, 1, '零售参数配置'),
(0, 'M000602', 'retail-out',    '零售出库', 1, '/business/retail/out',    'Business/Retail/Outbound/List',   'business:retail:out',    'ExportOutlined',  2, 1, 1, '零售出库单管理'),
(0, 'M000603', 'retail-return', '零售退货', 1, '/business/retail/return', 'Business/Retail/Return/List',     'business:retail:return', 'ImportOutlined',  3, 1, 1, '零售退货单管理')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), title=VALUES(title), menu_type=VALUES(menu_type), path=VALUES(path),
  component=VALUES(component), permission=VALUES(permission), icon=VALUES(icon),
  sort=VALUES(sort), visible=VALUES(visible), status=VALUES(status), remark=VALUES(remark), is_deleted=0;

-- ---------- 结算管理 ----------
INSERT INTO sys_menu (parent_id, code, name, title, menu_type, path, component, permission, icon, sort, visible, status, remark) VALUES
(0, 'M000701', 'settle-in-out-item',      '收支项目',       1, '/business/settle/item',                'Business/Settle/IncomeExpenseItem/List',    'business:settle:item',              'BarsOutlined',            1, 1, 1, '收入/支出项目维护'),
(0, 'M000702', 'settle-fee-sheet',        '供应商费用单',   1, '/business/settle/fee',                 'Business/Settle/FeeSheet/List',             'business:settle:fee',               'AccountBookOutlined',     2, 1, 1, '供应商费用单管理'),
(0, 'M000703', 'settle-pre-sheet',        '供应商预付款单', 1, '/business/settle/pre',                 'Business/Settle/PreSheet/List',             'business:settle:pre',               'MoneyCollectOutlined',    3, 1, 1, '供应商预付款单管理'),
(0, 'M000704', 'settle-check-sheet',      '供应商对账单',   1, '/business/settle/check',               'Business/Settle/CheckSheet/List',           'business:settle:check',             'ReconciliationOutlined',  4, 1, 1, '供应商对账单管理'),
(0, 'M000705', 'settle-sheet',            '供应商结算单',   1, '/business/settle/sheet',               'Business/Settle/SettleSheet/List',          'business:settle:sheet',             'TransactionOutlined',     5, 1, 1, '供应商结算单管理'),
(0, 'M000706', 'customer-fee-sheet',      '客户费用单',     1, '/business/settle/customer/fee',        'Business/Settle/CustomerFeeSheet/List',     'business:settle:customer:fee',      'AccountBookOutlined',     6, 1, 1, '客户费用单管理'),
(0, 'M000707', 'customer-pre-sheet',      '客户预收款单',   1, '/business/settle/customer/pre',        'Business/Settle/CustomerPreSheet/List',     'business:settle:customer:pre',      'MoneyCollectOutlined',    7, 1, 1, '客户预收款单管理'),
(0, 'M000708', 'customer-check-sheet',    '客户对账单',     1, '/business/settle/customer/check',      'Business/Settle/CustomerCheckSheet/List',   'business:settle:customer:check',    'ReconciliationOutlined',  8, 1, 1, '客户对账单管理'),
(0, 'M000709', 'customer-settle-sheet',   '客户结算单',     1, '/business/settle/customer/sheet',      'Business/Settle/CustomerSettleSheet/List',  'business:settle:customer:sheet',    'TransactionOutlined',     9, 1, 1, '客户结算单管理')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), title=VALUES(title), menu_type=VALUES(menu_type), path=VALUES(path),
  component=VALUES(component), permission=VALUES(permission), icon=VALUES(icon),
  sort=VALUES(sort), visible=VALUES(visible), status=VALUES(status), remark=VALUES(remark), is_deleted=0;

-- ---------- 统计报表 ----------
INSERT INTO sys_menu (parent_id, code, name, title, menu_type, path, component, permission, icon, sort, visible, status, remark) VALUES
(0, 'M000801', 'chart-dashboard', '数据看板',   1, '/chart/dashboard', 'Business/Chart/Dashboard', 'chart:dashboard', 'DashboardOutlined',    1, 1, 1, '经营数据看板'),
(0, 'M000802', 'chart-purchase',  '采购统计',   1, '/chart/purchase',  'Business/Chart/Purchase',  'chart:purchase',  'ShoppingCartOutlined', 2, 1, 1, '采购金额/数量统计'),
(0, 'M000803', 'chart-sales',     '销售统计',   1, '/chart/sales',     'Business/Chart/Sales',     'chart:sales',     'RiseOutlined',         3, 1, 1, '销售金额/数量统计'),
(0, 'M000804', 'chart-stock',     '库存报表',   1, '/chart/stock',     'Business/Chart/Stock',     'chart:stock',     'DatabaseOutlined',     4, 1, 1, '库存金额/库龄报表'),
(0, 'M000805', 'chart-summary',   '进销存汇总', 1, '/chart/summary',   'Business/Chart/Summary',   'chart:summary',   'FundOutlined',         5, 1, 1, '进销存综合分析')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), title=VALUES(title), menu_type=VALUES(menu_type), path=VALUES(path),
  component=VALUES(component), permission=VALUES(permission), icon=VALUES(icon),
  sort=VALUES(sort), visible=VALUES(visible), status=VALUES(status), remark=VALUES(remark), is_deleted=0;


-- =====================================================================
-- 3. 回填 parent_id（按 code → parent_code 映射，彻底消除孤儿节点）
-- =====================================================================
DROP TEMPORARY TABLE IF EXISTS tmp_menu_parent;
CREATE TEMPORARY TABLE tmp_menu_parent (
  code        VARCHAR(20) NOT NULL PRIMARY KEY,
  parent_code VARCHAR(20) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO tmp_menu_parent (code, parent_code) VALUES
-- 系统管理
('M000002','M000001'),('M000003','M000002'),('M000004','M000002'),('M000005','M000002'),('M000006','M000002'),
('M000007','M000001'),('M000008','M000007'),('M000009','M000007'),('M000010','M000007'),('M000011','M000007'),
('M000012','M000001'),('M000013','M000012'),('M000014','M000012'),('M000015','M000012'),
('M000016','M000001'),('M000017','M000016'),('M000018','M000016'),('M000019','M000016'),
('M000020','M000001'),('M000021','M000020'),('M000022','M000020'),('M000023','M000020'),
-- 基础数据
('M000101','M000100'),('M000102','M000100'),
('M000103','M000102'),('M000104','M000102'),('M000105','M000102'),
('M000106','M000100'),('M000107','M000100'),('M000108','M000100'),
('B000101','M000101'),('B000102','M000101'),('B000103','M000101'),('B000104','M000101'),
-- 库存管理
('M000201','M000200'),('M000202','M000200'),('M000203','M000200'),
('M000204','M000200'),('M000205','M000200'),('M000206','M000200'),
-- 采购管理
('M000301','M000300'),('M000302','M000300'),('M000303','M000300'),
-- 销售管理
('M000401','M000400'),('M000402','M000400'),('M000403','M000400'),
-- 零售管理
('M000601','M000600'),('M000602','M000600'),('M000603','M000600'),
-- 结算管理
('M000701','M000700'),('M000702','M000700'),('M000703','M000700'),('M000704','M000700'),('M000705','M000700'),
('M000706','M000700'),('M000707','M000700'),('M000708','M000700'),('M000709','M000700'),
-- 统计报表
('M000801','M000800'),('M000802','M000800'),('M000803','M000800'),('M000804','M000800'),('M000805','M000800');

UPDATE sys_menu c
JOIN tmp_menu_parent t ON t.code = c.code
LEFT JOIN sys_menu p   ON p.code = t.parent_code
SET c.parent_id = IFNULL(p.id, 0);

DROP TEMPORARY TABLE IF EXISTS tmp_menu_parent;


-- =====================================================================
-- 4. 修正历史遗留权限串
-- =====================================================================
UPDATE sys_menu SET permission = 'system:role:assign'
WHERE code = 'M000011' AND (permission = 'system:role:授权' OR permission IS NULL);


-- =====================================================================
-- 5. 给超级管理员(role_id=1)授权全部菜单（目录 + 菜单 + 按钮）
--    修复原先「只绑子菜单、不绑目录」导致菜单树断裂的问题
-- =====================================================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.id
FROM sys_menu m
WHERE m.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id
  );


-- =====================================================================
-- 6. 验证
-- =====================================================================
SELECT '=== 修复后：菜单总数（应为 71 条 = 9目录 + 41菜单 + 21按钮）===' AS info;
SELECT COUNT(*) AS menu_total FROM sys_menu WHERE is_deleted = 0;

SELECT '=== 修复后：孤儿节点复查（应为空）===' AS info;
SELECT m.id, m.code, m.title, m.parent_id
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.id
WHERE m.parent_id > 0 AND p.id IS NULL AND m.is_deleted = 0;

SELECT '=== 编码重复复查（应为空）===' AS info;
SELECT code, COUNT(*) c FROM sys_menu GROUP BY code HAVING c > 1;

SELECT '=== admin 授权覆盖检查（未授权菜单应为空）===' AS info;
SELECT m.id, m.code, m.title
FROM sys_menu m
WHERE m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);

SELECT '=== 最终菜单树 ===' AS info;
SELECT
  m.id, m.parent_id,
  IFNULL(p.title, '(顶级)') AS parent_title,
  m.code, m.title, m.menu_type AS t, m.path, m.permission, m.sort
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.id
WHERE m.is_deleted = 0
ORDER BY IFNULL(p.sort, m.sort), m.parent_id, m.sort, m.id;
