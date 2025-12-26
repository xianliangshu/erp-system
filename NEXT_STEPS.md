# ERP系统开发任务 - 下一步计划

## 上次开发完成内容 (2025-12-26)

### 已完成模块

| 模块 | 功能 | SQL文件 | 状态 |
|------|------|---------|------|
| 销售订单 | 创建、编辑、审核、拒绝、取消、删除 | `sale_order.sql` | ✅ 完成 |
| 销售出库 | 基于审核订单出库，确认时扣减库存 | `sale_delivery.sql` | ✅ 完成 |
| 销售退货 | 基于已确认出库单退货，确认时增加库存 | `sale_return.sql` | ✅ 完成 |
| 库存盘点 | 盘点录入、审核后调整库存(盈亏处理) | `stock_check.sql` | ✅ 完成 |
| 库存查询 | 修复关联信息显示问题 | - | ✅ 完成 |

---

## 下次开发任务

### 优先级 1: 库存调拨模块
- [ ] 后端: Entity、DTO、Mapper、Service、Controller
- [ ] 前端: API、列表页、表单页、详情页
- [ ] SQL: `stock_transfer.sql`
- [ ] 功能: 仓库间调拨，出库仓减库存，入库仓加库存

### 优先级 2: 报表统计模块
- [ ] 采购统计报表
- [ ] 销售统计报表
- [ ] 库存报表
- [ ] 进销存汇总表

### 优先级 3: 数据导出
- [ ] Excel导出功能
- [ ] 打印功能

### 优先级 4: 系统优化
- [ ] 菜单排序优化（库存管理放到销售管理下面）
- [ ] 页面样式统一
- [ ] 错误处理优化

---

## 执行说明

### 1. 执行SQL脚本
如果还未执行，请按顺序执行以下SQL文件：
```sql
-- 在MySQL中执行
source sql/sale_order.sql
source sql/sale_delivery.sql
source sql/sale_return.sql
source sql/stock_check.sql
source sql/fix_stock_check_menu.sql  -- 修复库存盘点菜单

-- 分配库存管理菜单给管理员角色
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 35);
```

### 2. 启动项目
```bash
# 后端
cd erp-parent
mvn clean compile
# 启动 ErpWebApplication

# 前端
cd erp-adminPro
npm run dev
```

### 3. 继续开发
告诉AI助手：`继续开发库存调拨模块` 或 `开发报表统计模块`

---

## Git仓库
- 远程仓库: https://github.com/xianliangshu/erp-system
- 最新提交: feat: 添加销售模块和库存盘点模块
