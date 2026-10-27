# ERP 系统 —— 项目长期约定（精简索引）

> **详细细节全部在 `.workbuddy/memory/REFERENCE.md`**，遇到具体问题去那里查。
> 本文件只放每次都要遵守的铁律。执行流水见 `YYYY-MM-DD.md`，设计见 `docs/`。

## 环境

- 多模块：`erp-common` / `erp-basedata` / `erp-business` / `erp-system` / `erp-web`（**全部 Controller 在此**）。
  前端 `erp-adminPro`（React+Vite+TS+antd5）。
- MySQL `erp-data`@192.168.31.199 root/123456，客户端 `/d/soft/mysql-8.4.10-winx64/bin/mysql.exe`
  **必带 `--default-character-set=utf8mb4`**。Redis 同机 6379/123456/db0。**context-path `/api`**。
- ⚠️ 启动后端必须先 `unset SERVER__PORT SERVER__HOST`，否则 Spring 宽松绑定覆盖 yml。
  **8080 是用户在 IDEA 的实例，别碰**；自建验证用 8081/8082。
- ⚠️ 前端验证用 `npx vite build`；`npm run build` 的 tsc 步骤是坏的。

## 铁律（违反必踩坑）

1. 🔴 **`base_material.id` 就是 SKU ID**，单据/库存表的 `product_id` 语义不变，13 张业务表零改动。
   **不要改成 `sku_id`。**
2. 🔴 **权限串必须已存在于 `sys_menu.permission`**，否则非超管永远 403 且授权页勾不到。
   **跨模块下拉数据源不加 `@SaCheckPermission`**（加了会让采购单「选仓库/选供应商」直接 403）。
3. 🔴 **鉴权异常回真实 HTTP 401/403**，不能回 200+code；白名单路径**不带 `/api`** 前缀。
4. 🔴 **出库不用 `addStock(负数)`**；传售价的入库必须走 `addStockKeepCost`（否则污染成本价）。
   扣库存前按**商品汇总**后 `checkStockEnough`。
5. 🔴 **成本调整只改价不改量**，直接 `UPDATE product_stock`，绝不走 `addStock`/`subStock`。
6. 🔴 **唯一键不含 `deleted` 的表禁止用逻辑删除**（SKU 关联表、`stock_warning` 等），
   否则删除后重建必撞唯一键。建表脚本必须补齐实体基类的全部公共列（明细表除外）。
7. 🔴 **主数据编号流水取「含已逻辑删除记录」的全表最大值**，否则 `Duplicate entry` 500。
   列名是 `seq_date`，不是 `current_date`（保留字）。
8. **顶级菜单 `parent_id` 是 `0` 不是 `NULL`**；授权必须含所有上级目录，否则整棵子树消失。
9. **`StpInterface` 每次鉴权都调用** → 必须走 `SaPermissionCache`；授权变更后 `evictAll()/evictUser()`。
   Redis key 前缀是 `Authorization:`（取自 token-name），**不是 `satoken`**。
10. **授权/菜单脚本执行后清 `erp:auth:*` 缓存**。
11. 🔴 **`@RequestBody` 接明细数组一律用强类型 DTO，别用 `Map<String,Object>`**：
    Jackson 还原成 `List<LinkedHashMap>`，按实体迭代时 `ClassCastException`→500（泛型擦除让强转编译期不报错）。

## 常见坑

- ⚠️ **前端只跑 `vite build` 不算验证**：esbuild 不做类型检查，改错变量名照样构建成功、
  页面**静默不发请求**（异常被 catch 吞掉）⇒ 必须再跑 **`npx tsc --noEmit`** 并 grep `Cannot find name`。
- ⚠️ bash 的 `env` 不是 coreutils 的（会静默吞 `-u` 参数后退出，表现为「应用没启动但退出码 0」）。
- ⚠️ Git Bash 会把 `/system/xxx` 形式的命令行参数转成 Windows 路径（CDP 导航要加 `MSYS_NO_PATHCONV=1`）。
- ⚠️ 未匹配 URL 被 `GlobalExceptionHandler` 兜底成 **HTTP 200 + code 500**（非 404）。
- ⚠️ `multipart.max-file-size` 默认 1MB，已放宽 10MB/20MB。
- ⚠️ 改库前先 `mysqldump` 备份到 `.workbuddy/backup/`。
- 前端下载二进制**不能走 `utils/request`**，用 `utils/download.ts`。

## 索引

| 想了解 | 去哪 |
|---|---|
| Sa-Token 架构、224 个权限注解映射规则、白名单清单 | 参考 §3 |
| 编号规则 / Excel 导入导出 / 日志体系 | 参考 §4 |
| 实体基类与建表脚本对应关系、自查脚本 | 参考 §5 |
| 库存变动三入口语义 | 参考 §6 |
| 已知欠账（含 7 个未修的正确性问题） | 参考 §7 |
| 开发验证套路、无头 Chrome CDP 取证法 | 参考 §8 |
| 商品中心 SPU/SKU 模型与落地细节 | 参考 §9 |
| 库存体系补齐两段（盘点参数/预警/成本调整 + 盘点任务/预先盘点单） | 参考 §10 |
| **前端列表页 query state 收敛写法（已落地 34 个列表页）** | 参考 §11 |
