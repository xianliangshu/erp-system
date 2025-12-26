-- 修复库存盘点菜单插入
-- 先查看当前库存管理相关的菜单结构

-- 方法1: 如果库存查询菜单存在，获取其父ID
-- SELECT * FROM sys_menu WHERE path LIKE '%stock%';

-- 直接插入库存盘点菜单（假设库存管理目录ID已知，需根据实际情况调整）
-- 首先查找库存查询菜单的parent_id
SET @stock_query_parent = (SELECT parent_id FROM sys_menu WHERE path = '/business/stock/query' LIMIT 1);

-- 如果没有找到，则直接创建一个新的库存管理目录
-- 请先执行查询确认 parent_id：SELECT * FROM sys_menu WHERE path = '/business/stock/query';

-- 插入库存盘点菜单
INSERT INTO `sys_menu` (`parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `icon`, `sort`, `visible`, `status`, `permission`, `component`, `remark`) 
VALUES (@stock_query_parent, 'M000502', 'stock-check', '库存盘点', 1, '/business/stock/check', 'AuditOutlined', 2, 1, 1, 'business:stock:check', '/Business/Stock/Check/List', '库存盘点管理')
ON DUPLICATE KEY UPDATE title = '库存盘点';

SET @stock_check_menu_id = LAST_INSERT_ID();

-- 如果LAST_INSERT_ID()为0，说明是更新操作，需要重新查询ID
SET @stock_check_menu_id = IF(@stock_check_menu_id = 0, (SELECT id FROM sys_menu WHERE path = '/business/stock/check' LIMIT 1), @stock_check_menu_id);

-- 分配给管理员角色
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, @stock_check_menu_id);

-- 验证查询
-- SELECT * FROM sys_menu WHERE path = '/business/stock/check';
-- SELECT * FROM sys_role_menu WHERE menu_id = @stock_check_menu_id;
