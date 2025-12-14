---
description: ERP系统后端项目结构和技术栈说明
---

# ERP系统后端项目概览

## 项目信息

- **项目名称**: ERP电商管理系统
- **技术栈**: Spring Boot 3.0.2 + MyBatis-Plus + MySQL 8.0
- **Java版本**: 17
- **构建工具**: Maven
- **项目类型**: 多模块Maven项目

## 模块结构

```
erp-parent/
├── erp-common/          # 公共模块 (工具类、异常、结果封装)
├── erp-system/          # 系统模块 (用户、角色、部门、菜单)
├── erp-business/        # 业务模块 (预留)
└── erp-web/            # Web模块 (Controller层)
```

## 核心依赖

### erp-common
- spring-boot-starter-web
- lombok
- hutool-all (5.8.16)
- spring-security-crypto (6.0.1) - BCrypt密码加密

### erp-system
- erp-common
- mybatis-plus-boot-starter (3.5.7)
- mysql-connector-j
- spring-boot-starter-data-redis
- knife4j-openapi3-jakarta-spring-boot-starter (4.1.0)

### erp-web
- erp-system
- erp-business
- knife4j-openapi3-jakarta-spring-boot-starter

## 已实现模块

### 1. 用户管理 (SysUser)
- **Service**: `SysUserServiceImpl.java`
- **Controller**: `SysUserController.java`
- **功能**: CRUD、密码管理、角色分配、部门分配
- **编号格式**: U000001

### 2. 角色管理 (SysRole)
- **Service**: `SysRoleServiceImpl.java`
- **Controller**: `SysRoleController.java`
- **功能**: CRUD、菜单权限分配、用户统计
- **编号格式**: R000001

### 3. 部门管理 (SysDept)
- **Service**: `SysDeptServiceImpl.java`
- **Controller**: `SysDeptController.java`
- **功能**: CRUD、树形结构、祖级列表维护
- **编号格式**: D000001

### 4. 菜单管理 (SysMenu)
- **Service**: `SysMenuServiceImpl.java`
- **Controller**: `SysMenuController.java`
- **功能**: CRUD、树形结构、用户菜单查询
- **编号格式**: M000001

## 数据库表

- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_dept` - 部门表
- `sys_menu` - 菜单表
- `sys_user_role` - 用户角色关联表
- `sys_user_dept` - 用户部门关联表
- `sys_role_menu` - 角色菜单关联表
- `sys_dict_type` - 字典类型表
- `sys_dict_data` - 字典数据表
- `sys_login_log` - 登录日志表
- `sys_operation_log` - 操作日志表

## 关键特性

1. **编号自动生成**: 所有实体都有自动生成的编号 (前缀+6位数字)
2. **密码加密**: 使用BCrypt加密存储密码
3. **逻辑删除**: 使用MyBatis-Plus的@TableLogic
4. **自动填充**: 创建时间、更新时间、创建人、更新人
5. **树形结构**: 部门和菜单支持树形结构
6. **关联管理**: 用户-角色-部门-菜单的多对多关联

## API文档

- **访问地址**: http://localhost:8080/doc.html
- **工具**: Knife4j (Swagger增强版)
- **接口总数**: 35个

## 前端项目

- **目录**: erp-adminPro/
- **技术栈**: React 18 + Vite + Ant Design
- **状态管理**: Zustand
- **路由**: React Router DOM
