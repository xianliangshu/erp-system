-- ============================================================
-- 跨境电商ERP系统 - 用户管理模块数据库设计
-- 版本: 1.0
-- 创建日期: 2025-12-12
-- 说明: 基于星云ERP参考设计,适配Spring Boot 3.2.0 + MySQL 8.0
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 用户表 (sys_user)
-- ============================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `code` VARCHAR(20) NOT NULL COMMENT '用户编号',
  `username` VARCHAR(30) NOT NULL COMMENT '用户名(登录账号)',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` VARCHAR(50) NULL DEFAULT NULL COMMENT '昵称',
  `real_name` VARCHAR(50) NULL DEFAULT NULL COMMENT '真实姓名',
  `email` VARCHAR(100) NULL DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) NULL DEFAULT NULL COMMENT '手机号',
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
  `avatar` VARCHAR(500) NULL DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
  `remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_login_time` DATETIME NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) NULL DEFAULT NULL COMMENT '最后登录IP',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 初始化管理员账号 (密码: admin123)
INSERT INTO `sys_user` (`id`, `code`, `username`, `password`, `nickname`, `real_name`, `email`, `phone`, `gender`, `status`, `create_by`, `create_time`)
VALUES (1, 'U000001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '管理员', 'admin@erp.com', '13800138000', 1, 1, 'system', NOW());

-- ============================================================
-- 2. 角色表 (sys_role)
-- ============================================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `code` VARCHAR(20) NOT NULL COMMENT '角色编号',
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `permission_code` VARCHAR(100) NULL DEFAULT NULL COMMENT '权限字符串(用于注解鉴权)',
  `data_scope` TINYINT NOT NULL DEFAULT 1 COMMENT '数据权限范围: 1-全部 2-本部门及以下 3-本部门 4-仅本人',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
  `remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- 初始化角色数据
INSERT INTO `sys_role` (`id`, `code`, `name`, `permission_code`, `data_scope`, `status`, `remark`, `create_by`, `create_time`)
VALUES 
(1, 'R000001', '超级管理员', 'admin', 1, 1, '拥有系统所有权限', 'system', NOW()),
(2, 'R000002', '采购经理', 'purchase_manager', 2, 1, '采购管理权限', 'system', NOW()),
(3, 'R000003', '销售经理', 'sales_manager', 2, 1, '销售管理权限', 'system', NOW()),
(4, 'R000004', '仓库管理员', 'warehouse_admin', 3, 1, '仓库管理权限', 'system', NOW());

-- ============================================================
-- 3. 部门表 (sys_dept)
-- ============================================================
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `parent_id` BIGINT NULL DEFAULT NULL COMMENT '父部门ID(顶级为NULL)',
  `ancestors` VARCHAR(500) NULL DEFAULT NULL COMMENT '祖级列表(逗号分隔)',
  `code` VARCHAR(20) NOT NULL COMMENT '部门编号',
  `name` VARCHAR(50) NOT NULL COMMENT '部门名称',
  `short_name` VARCHAR(20) NULL DEFAULT NULL COMMENT '部门简称',
  `leader` VARCHAR(50) NULL DEFAULT NULL COMMENT '负责人',
  `phone` VARCHAR(20) NULL DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(100) NULL DEFAULT NULL COMMENT '邮箱',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
  `remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门表';

-- 初始化部门数据
INSERT INTO `sys_dept` (`id`, `parent_id`, `ancestors`, `code`, `name`, `short_name`, `sort`, `status`, `create_by`, `create_time`)
VALUES 
(1, NULL, '0', 'D000001', '总公司', '总公司', 1, 1, 'system', NOW()),
(2, 1, '0,1', 'D000002', '采购部', '采购部', 1, 1, 'system', NOW()),
(3, 1, '0,1', 'D000003', '销售部', '销售部', 2, 1, 'system', NOW()),
(4, 1, '0,1', 'D000004', '仓储部', '仓储部', 3, 1, 'system', NOW()),
(5, 1, '0,1', 'D000005', '财务部', '财务部', 4, 1, 'system', NOW());

-- ============================================================
-- 4. 菜单表 (sys_menu)
-- ============================================================
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` BIGINT NULL DEFAULT NULL COMMENT '父菜单ID(顶级为NULL)',
  `code` VARCHAR(20) NOT NULL COMMENT '菜单编号',
  `name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `title` VARCHAR(50) NOT NULL COMMENT '菜单标题(显示名称)',
  `menu_type` TINYINT NOT NULL COMMENT '菜单类型: 0-目录 1-菜单 2-按钮',
  `path` VARCHAR(200) NULL DEFAULT NULL COMMENT '路由地址',
  `component` VARCHAR(200) NULL DEFAULT NULL COMMENT '组件路径',
  `permission` VARCHAR(100) NULL DEFAULT NULL COMMENT '权限标识',
  `icon` VARCHAR(100) NULL DEFAULT NULL COMMENT '菜单图标',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示: 0-隐藏 1-显示',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
  `remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单表';

-- 初始化菜单数据
INSERT INTO `sys_menu` (`id`, `parent_id`, `code`, `name`, `title`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `create_by`)
VALUES 
-- 系统管理目录
(1, NULL, 'M000001', 'System', '系统管理', 0, '/system', NULL, NULL, 'ant-design:setting-outlined', 100, 1, 1, 'system'),
-- 用户管理菜单
(2, 1, 'M000002', 'User', '用户管理', 1, '/system/user', '/system/user/index', 'system:user:list', 'ant-design:user-outlined', 1, 1, 1, 'system'),
(3, 2, 'M000003', '', '新增用户', 2, NULL, NULL, 'system:user:add', NULL, 1, 1, 1, 'system'),
(4, 2, 'M000004', '', '修改用户', 2, NULL, NULL, 'system:user:edit', NULL, 2, 1, 1, 'system'),
(5, 2, 'M000005', '', '删除用户', 2, NULL, NULL, 'system:user:delete', NULL, 3, 1, 1, 'system'),
(6, 2, 'M000006', '', '重置密码', 2, NULL, NULL, 'system:user:resetPwd', NULL, 4, 1, 1, 'system'),
-- 角色管理菜单
(7, 1, 'M000007', 'Role', '角色管理', 1, '/system/role', '/system/role/index', 'system:role:list', 'ant-design:team-outlined', 2, 1, 1, 'system'),
(8, 7, 'M000008', '', '新增角色', 2, NULL, NULL, 'system:role:add', NULL, 1, 1, 1, 'system'),
(9, 7, 'M000009', '', '修改角色', 2, NULL, NULL, 'system:role:edit', NULL, 2, 1, 1, 'system'),
(10, 7, 'M000010', '', '删除角色', 2, NULL, NULL, 'system:role:delete', NULL, 3, 1, 1, 'system'),
(11, 7, 'M000011', '', '分配权限', 2, NULL, NULL, 'system:role:授权', NULL, 4, 1, 1, 'system'),
-- 部门管理菜单
(12, 1, 'M000012', 'Dept', '部门管理', 1, '/system/dept', '/system/dept/index', 'system:dept:list', 'ant-design:apartment-outlined', 3, 1, 1, 'system'),
(13, 12, 'M000013', '', '新增部门', 2, NULL, NULL, 'system:dept:add', NULL, 1, 1, 1, 'system'),
(14, 12, 'M000014', '', '修改部门', 2, NULL, NULL, 'system:dept:edit', NULL, 2, 1, 1, 'system'),
(15, 12, 'M000015', '', '删除部门', 2, NULL, NULL, 'system:dept:delete', NULL, 3, 1, 1, 'system'),
-- 菜单管理菜单
(16, 1, 'M000016', 'Menu', '菜单管理', 1, '/system/menu', '/system/menu/index', 'system:menu:list', 'ant-design:menu-outlined', 4, 1, 1, 'system'),
(17, 16, 'M000017', '', '新增菜单', 2, NULL, NULL, 'system:menu:add', NULL, 1, 1, 1, 'system'),
(18, 16, 'M000018', '', '修改菜单', 2, NULL, NULL, 'system:menu:edit', NULL, 2, 1, 1, 'system'),
(19, 16, 'M000019', '', '删除菜单', 2, NULL, NULL, 'system:menu:delete', NULL, 3, 1, 1, 'system');

-- ============================================================
-- 5. 用户角色关联表 (sys_user_role)
-- ============================================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- 初始化管理员角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- ============================================================
-- 6. 用户部门关联表 (sys_user_dept)
-- ============================================================
DROP TABLE IF EXISTS `sys_user_dept`;
CREATE TABLE `sys_user_dept` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `dept_id` BIGINT NOT NULL COMMENT '部门ID',
  `is_main` TINYINT NOT NULL DEFAULT 0 COMMENT '是否主部门: 0-否 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_dept` (`user_id`, `dept_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户部门关联表';

-- 初始化管理员部门关联
INSERT INTO `sys_user_dept` (`user_id`, `dept_id`, `is_main`) VALUES (1, 1, 1);

-- ============================================================
-- 7. 角色菜单关联表 (sys_role_menu)
-- ============================================================
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关联表';

-- ============================================================
-- 8. 操作日志表 (sys_operation_log)
-- ============================================================
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `title` VARCHAR(50) NULL DEFAULT NULL COMMENT '操作模块',
  `business_type` TINYINT NOT NULL DEFAULT 0 COMMENT '业务类型: 0-其它 1-新增 2-修改 3-删除 4-查询 5-导出 6-导入',
  `method` VARCHAR(200) NULL DEFAULT NULL COMMENT '方法名称',
  `request_method` VARCHAR(10) NULL DEFAULT NULL COMMENT '请求方式',
  `operator_type` TINYINT NOT NULL DEFAULT 0 COMMENT '操作类别: 0-其它 1-后台用户 2-手机端用户',
  `oper_name` VARCHAR(50) NULL DEFAULT NULL COMMENT '操作人员',
  `oper_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '请求URL',
  `oper_ip` VARCHAR(50) NULL DEFAULT NULL COMMENT '操作地址',
  `oper_location` VARCHAR(255) NULL DEFAULT NULL COMMENT '操作地点',
  `oper_param` TEXT NULL COMMENT '请求参数',
  `json_result` TEXT NULL COMMENT '返回结果',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '操作状态: 0-正常 1-异常',
  `error_msg` TEXT NULL COMMENT '错误消息',
  `oper_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `cost_time` BIGINT NOT NULL DEFAULT 0 COMMENT '消耗时间(毫秒)',
  PRIMARY KEY (`id`),
  KEY `idx_oper_time` (`oper_time`),
  KEY `idx_business_type` (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- ============================================================
-- 9. 登录日志表 (sys_login_log)
-- ============================================================
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `username` VARCHAR(50) NULL DEFAULT NULL COMMENT '用户账号',
  `login_ip` VARCHAR(50) NULL DEFAULT NULL COMMENT '登录IP',
  `login_location` VARCHAR(255) NULL DEFAULT NULL COMMENT '登录地点',
  `browser` VARCHAR(50) NULL DEFAULT NULL COMMENT '浏览器类型',
  `os` VARCHAR(50) NULL DEFAULT NULL COMMENT '操作系统',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '登录状态: 0-成功 1-失败',
  `msg` VARCHAR(255) NULL DEFAULT NULL COMMENT '提示消息',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表';

-- ============================================================
-- 10. 字典类型表 (sys_dict_type)
-- ============================================================
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
  `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
  `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典类型表';

-- 初始化字典类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`, `create_by`)
VALUES 
('用户性别', 'sys_user_gender', 1, '用户性别列表', 'system'),
('用户状态', 'sys_user_status', 1, '用户状态列表', 'system'),
('菜单类型', 'sys_menu_type', 1, '菜单类型列表', 'system'),
('数据权限范围', 'sys_data_scope', 1, '数据权限范围列表', 'system');

-- ============================================================
-- 11. 字典数据表 (sys_dict_data)
-- ============================================================
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` INT NOT NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签',
  `dict_value` VARCHAR(100) NOT NULL COMMENT '字典键值',
  `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
  `css_class` VARCHAR(100) NULL DEFAULT NULL COMMENT '样式属性',
  `list_class` VARCHAR(100) NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认: 0-否 1-是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典数据表';

-- 初始化字典数据
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`)
VALUES 
-- 用户性别
(1, '未知', '0', 'sys_user_gender', 'default', 1, 1, 'system'),
(2, '男', '1', 'sys_user_gender', 'primary', 0, 1, 'system'),
(3, '女', '2', 'sys_user_gender', 'danger', 0, 1, 'system'),
-- 用户状态
(1, '禁用', '0', 'sys_user_status', 'danger', 0, 1, 'system'),
(2, '启用', '1', 'sys_user_status', 'success', 1, 1, 'system'),
-- 菜单类型
(1, '目录', '0', 'sys_menu_type', 'primary', 0, 1, 'system'),
(2, '菜单', '1', 'sys_menu_type', 'success', 0, 1, 'system'),
(3, '按钮', '2', 'sys_menu_type', 'warning', 0, 1, 'system'),
-- 数据权限范围
(1, '全部数据权限', '1', 'sys_data_scope', 'primary', 0, 1, 'system'),
(2, '本部门及以下数据权限', '2', 'sys_data_scope', 'success', 0, 1, 'system'),
(3, '本部门数据权限', '3', 'sys_data_scope', 'warning', 0, 1, 'system'),
(4, '仅本人数据权限', '4', 'sys_data_scope', 'danger', 0, 1, 'system');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 数据库设计说明
-- ============================================================
/*
1. 主键设计:
   - 使用BIGINT自增主键,适合大数据量
   - 业务编号(code)作为唯一索引,便于业务查询

2. 字段规范:
   - 所有表统一使用utf8mb4字符集
   - 时间字段使用DATETIME类型
   - 状态字段使用TINYINT类型
   - 软删除使用is_deleted字段

3. 索引设计:
   - 主键索引: id
   - 唯一索引: code, username等业务唯一字段
   - 普通索引: 外键字段、状态字段、时间字段

4. 审计字段:
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 更新人
   - update_time: 更新时间

5. 权限设计:
   - RBAC模型: 用户-角色-菜单
   - 数据权限: 通过data_scope字段控制
   - 菜单权限: 通过permission字段控制

6. 扩展性:
   - 用户可关联多个角色
   - 用户可关联多个部门
   - 角色可关联多个菜单
   - 部门支持树形结构

7. 跨境电商特色:
   - 预留多语言支持
   - 预留多币种支持
   - 预留多仓库支持
*/
