-- MySQL dump 10.13  Distrib 8.4.10, for Win64 (x86_64)
--
-- Host: 192.168.31.199    Database: erp-data
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `sys_menu`
--

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (1,0,'M0002','inventory','库存管理',0,'/inventory',NULL,NULL,'StockOutlined',2,1,1,0,'库存管理模块',NULL,'2026-08-01 19:14:40',NULL,'2026-08-01 19:14:40'),(2,1,'M000201','stock-query','库存查询',1,'/business/stock/query','/Business/Stock/Query','business:stock:query','SearchOutlined',1,1,1,0,'实时库存查询',NULL,'2026-08-01 19:14:40',NULL,'2026-08-01 19:14:40'),(3,0,'M0003','purchase','采购管理',0,'/purchase',NULL,NULL,'ShoppingCartOutlined',3,1,1,0,'采购管理模块',NULL,'2026-08-01 19:14:55',NULL,'2026-08-01 19:14:55'),(4,3,'M000301','purchase-order','采购订单',1,'/business/purchase/order','/Business/Purchase/Order/List','business:purchase:order','FileTextOutlined',1,1,1,0,'采购订单管理',NULL,'2026-08-01 19:14:55',NULL,'2026-08-01 19:14:55'),(5,20,'M000102','material','物料管理',0,'/basedata/material',NULL,NULL,'shopping',2,1,1,0,'物料管理模块',NULL,'2026-08-01 19:16:01',NULL,'2026-08-01 19:16:01'),(6,5,'M000103','material-category','物料分类',1,'/basedata/material/category','Basedata/Material/Category','basedata:material:category','folder-tree',1,1,1,0,'物料分类管理',NULL,'2026-08-01 19:16:01',NULL,'2026-08-01 19:16:01'),(7,5,'M000104','unit','计量单位',1,'/basedata/material/unit','Basedata/Material/Unit','basedata:material:unit','calculator',2,1,1,0,'计量单位管理',NULL,'2026-08-01 19:16:01',NULL,'2026-08-01 19:16:01'),(8,5,'M000105','material-info','物料信息',1,'/basedata/material/info','Basedata/Material/Info','basedata:material:info','box',3,1,1,0,'物料信息管理',NULL,'2026-08-01 19:16:01',NULL,'2026-08-01 19:16:01'),(9,0,'M000100','Basedata','基础数据',0,'/basedata',NULL,NULL,'ant-design:database-outlined',2,1,1,0,'基础数据管理模块',NULL,'2026-08-01 19:16:12',NULL,'2026-08-01 19:16:12'),(10,9,'M000101','Warehouse','仓库管理',1,'/basedata/warehouse','basedata/warehouse/index','basedata:warehouse:list','ant-design:home-outlined',1,1,1,0,'仓库信息管理',NULL,'2026-08-01 19:16:12',NULL,'2026-08-01 19:16:12'),(11,10,'B000101','WarehouseAdd','新增仓库',2,NULL,NULL,'basedata:warehouse:add',NULL,1,1,1,0,'新增仓库按钮',NULL,'2026-08-01 19:16:12',NULL,'2026-08-01 19:16:12'),(12,10,'B000102','WarehouseEdit','编辑仓库',2,NULL,NULL,'basedata:warehouse:edit',NULL,2,1,1,0,'编辑仓库按钮',NULL,'2026-08-01 19:16:12',NULL,'2026-08-01 19:16:12'),(13,10,'B000103','WarehouseDelete','删除仓库',2,NULL,NULL,'basedata:warehouse:delete',NULL,3,1,1,0,'删除仓库按钮',NULL,'2026-08-01 19:16:12',NULL,'2026-08-01 19:16:12'),(14,10,'B000104','WarehouseSetDefault','设置默认',2,NULL,NULL,'basedata:warehouse:setDefault',NULL,4,1,1,0,'设置默认仓库按钮',NULL,'2026-08-01 19:16:12',NULL,'2026-08-01 19:16:12'),(15,3,'M000303','purchase-return','采购退货',1,'/business/purchase/return','/Business/Purchase/Return/List','business:purchase:return','RollbackOutlined',3,1,1,0,'采购退货管理',NULL,'2026-08-01 19:21:34',NULL,'2026-08-01 19:21:34'),(16,27,'M000106','supplier','供应商管理',1,'/basedata/supplier','/Basedata/Supplier','basedata:supplier:list','TeamOutlined',5,1,1,0,'供应商信息管理',NULL,'2026-08-01 19:22:06',NULL,'2026-08-01 19:22:06');
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `sys_role_menu`
--

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (1,1,5,'2026-08-01 19:16:01'),(2,1,6,'2026-08-01 19:16:01'),(3,1,7,'2026-08-01 19:16:01'),(4,1,8,'2026-08-01 19:16:01'),(8,1,2,'2026-08-01 19:21:14'),(9,1,3,'2026-08-01 19:21:25'),(10,1,4,'2026-08-01 19:21:25'),(11,1,15,'2026-08-01 19:21:34'),(12,1,16,'2026-08-01 19:22:06');
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-01 15:46:23
