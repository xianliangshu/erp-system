---
description: ERP系统数据库设计规范和表结构约定
---

# 数据库设计规范

## 数据库信息

- **数据库类型**: MySQL 8.0
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **引擎**: InnoDB

## 命名规范

### 表命名
- 全部小写，使用下划线分隔
- 使用模块前缀: `sys_`, `biz_`
- 使用单数形式
- 示例: `sys_user`, `sys_role`, `sys_dept`

### 字段命名
- 全部小写，使用下划线分隔
- 布尔字段使用 `is_` 前缀
- 时间字段使用 `_time` 后缀
- 示例: `user_name`, `is_deleted`, `create_time`

### 索引命名
- 主键: `pk_表名`
- 唯一索引: `uk_表名_字段名`
- 普通索引: `idx_表名_字段名`
- 示例: `pk_sys_user`, `uk_sys_user_username`, `idx_sys_user_status`

## 字段规范

### 主键字段
```sql
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
PRIMARY KEY (id)
```

### 编号字段
```sql
code VARCHAR(20) NOT NULL COMMENT '编号',
UNIQUE KEY uk_code (code)
```

### 状态字段
```sql
status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用'
```

### 逻辑删除字段
```sql
is_deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除'
```

### 审计字段
```sql
create_by VARCHAR(50) COMMENT '创建人',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_by VARCHAR(50) COMMENT '更新人',
update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
```

## 表结构示例

### 用户表
```sql
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    code VARCHAR(20) NOT NULL COMMENT '用户编号',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    gender TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    is_deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    remark VARCHAR(500) COMMENT '备注',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 关联表
```sql
CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
```

## 数据类型规范

### 整数类型
- `TINYINT` - 状态、标志位 (0-255)
- `INT` - 普通整数
- `BIGINT` - ID、大数值

### 字符串类型
- `VARCHAR(20)` - 编号、代码
- `VARCHAR(50)` - 用户名、姓名
- `VARCHAR(100)` - 邮箱、密码
- `VARCHAR(255)` - URL、路径
- `VARCHAR(500)` - 备注、描述
- `TEXT` - 长文本

### 时间类型
- `DATETIME` - 日期时间 (推荐)
- `TIMESTAMP` - 时间戳
- `DATE` - 日期

### 数值类型
- `DECIMAL(10,2)` - 金额、价格

## 索引设计规范

### 主键索引
- 每个表必须有主键
- 使用自增ID作为主键
- 不使用业务字段作为主键

### 唯一索引
- 唯一性约束字段必须建立唯一索引
- 示例: username, phone, code

### 普通索引
- 经常作为查询条件的字段
- 经常作为排序字段
- 关联查询的外键字段

### 联合索引
- 遵循最左前缀原则
- 区分度高的字段放在前面
- 示例: `idx_user_status_create_time (status, create_time)`

## 树形结构设计

### 部门树
```sql
CREATE TABLE sys_dept (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    parent_id BIGINT COMMENT '父部门ID',
    ancestors VARCHAR(500) COMMENT '祖级列表(逗号分隔)',
    code VARCHAR(20) NOT NULL COMMENT '部门编号',
    name VARCHAR(50) NOT NULL COMMENT '部门名称',
    sort INT DEFAULT 0 COMMENT '显示顺序',
    -- 其他字段...
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_ancestors (ancestors(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';
```

### 菜单树
```sql
CREATE TABLE sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT COMMENT '父菜单ID',
    code VARCHAR(20) NOT NULL COMMENT '菜单编号',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type TINYINT DEFAULT 0 COMMENT '菜单类型: 0-目录 1-菜单 2-按钮',
    path VARCHAR(200) COMMENT '路由地址',
    sort INT DEFAULT 0 COMMENT '显示顺序',
    -- 其他字段...
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';
```

## 数据初始化

### 管理员用户
```sql
INSERT INTO sys_user (code, username, password, nickname, status) 
VALUES ('U000001', 'admin', '$2a$10$...', '管理员', 1);
```

### 默认角色
```sql
INSERT INTO sys_role (code, name, permission_code, status) 
VALUES ('R000001', '超级管理员', 'admin', 1);
```

## 性能优化

### 分区表
- 大表使用分区提升查询性能
- 按时间分区: 日志表、历史数据表

### 读写分离
- 主库写入
- 从库查询

### 缓存策略
- 字典数据缓存
- 菜单树缓存
- 部门树缓存

## 数据安全

### 敏感数据加密
- 密码使用BCrypt加密
- 手机号可选脱敏显示
- 身份证号加密存储

### 数据备份
- 每天定时备份
- 保留最近30天备份
- 重要操作前手动备份

### 权限控制
- 最小权限原则
- 生产环境只读账号
- 审计日志记录

## 最佳实践

1. **字段要有默认值**: 避免NULL值
2. **使用NOT NULL**: 尽量使用NOT NULL约束
3. **字段要有注释**: 每个字段都要有清晰的注释
4. **合理使用索引**: 不要过度索引
5. **避免大字段**: TEXT、BLOB字段单独存储
6. **使用InnoDB引擎**: 支持事务和外键
7. **字符集统一**: 使用utf8mb4
8. **时间字段**: 使用DATETIME类型
9. **逻辑删除**: 重要数据使用逻辑删除
10. **审计字段**: 关键表要有审计字段
