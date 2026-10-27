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
-- Table structure for table `base_warehouse`
--

DROP TABLE IF EXISTS `base_warehouse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(20) NOT NULL COMMENT '仓库编号',
  `name` varchar(50) NOT NULL COMMENT '仓库名称',
  `contact` varchar(20) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(200) DEFAULT NULL COMMENT '仓库地址',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认仓库: 0-否 1-是',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_warehouse`
--

LOCK TABLES `base_warehouse` WRITE;
/*!40000 ALTER TABLE `base_warehouse` DISABLE KEYS */;
INSERT INTO `base_warehouse` VALUES (1,'WH001','主仓库','张三','13800138000','北京市朝阳区xxx路xxx号',1,1,'公司主仓库',NULL,'2026-08-01 19:24:35',NULL,'2026-08-01 19:24:35',0),(2,'WH002','分仓库A','李四','13800138001','上海市浦东新区xxx路xxx号',0,1,'上海分仓',NULL,'2026-08-01 19:24:35',NULL,'2026-08-01 19:24:35',0),(3,'WH003','分仓库B','王五','13800138002','广州市天河区xxx路xxx号',0,1,'广州分仓',NULL,'2026-08-01 19:24:35',NULL,'2026-08-01 19:24:35',0),(4,'WH000004','广州仓库','张三',NULL,NULL,0,1,'广州仓库','system','2026-10-01 19:48:07','system','2026-10-01 19:48:07',0);
/*!40000 ALTER TABLE `base_warehouse` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_code_rule`
--

DROP TABLE IF EXISTS `sys_code_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_code_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_code` varchar(64) NOT NULL COMMENT '规则编码(业务唯一标识,如 PURCHASE_ORDER)',
  `rule_name` varchar(100) NOT NULL COMMENT '单据/对象名称',
  `prefix` varchar(20) NOT NULL DEFAULT '' COMMENT '编号前缀',
  `date_format` varchar(20) NOT NULL DEFAULT '' COMMENT '日期段格式(yyyyMMdd),空=编号不含日期段',
  `seq_length` int NOT NULL DEFAULT '4' COMMENT '流水号位数',
  `seq_start` int NOT NULL DEFAULT '1' COMMENT '起始流水值',
  `seq_step` int NOT NULL DEFAULT '1' COMMENT '步长',
  `current_value` int NOT NULL DEFAULT '0' COMMENT '当前流水值(0=未初始化)',
  `seq_date` varchar(20) NOT NULL DEFAULT '' COMMENT '流水号所属日期(yyyyMMdd,日切判定)。注意: 不可命名为 current_date, 它是 MySQL 保留字',
  `reset_by_date` tinyint NOT NULL DEFAULT '1' COMMENT '1=按日重置 0=不重置(主数据编码用0)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=启用 0=停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除(与 erp-system 模块一致)',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单据编号规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_code_rule`
--

LOCK TABLES `sys_code_rule` WRITE;
/*!40000 ALTER TABLE `sys_code_rule` DISABLE KEYS */;
INSERT INTO `sys_code_rule` VALUES (1,'PURCHASE_ORDER','采购订单','PO','yyyyMMdd',4,1,1,1,'20261001',1,1,'原 PurchaseOrderServiceImpl',0,NULL,NULL,'system','2026-10-03 12:30:39'),(2,'PURCHASE_RECEIPT','采购收货单','PR','yyyyMMdd',4,1,1,1,'20261001',1,1,'原 PurchaseReceiptServiceImpl',0,NULL,NULL,NULL,NULL),(3,'PURCHASE_RETURN','采购退货单','PTH','yyyyMMdd',4,1,1,1,'20261003',1,1,'原 PurchaseReturnServiceImpl',0,NULL,NULL,'system','2026-10-03 13:49:45'),(4,'SALE_ORDER','销售订单','SO','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleOrderServiceImpl',0,NULL,NULL,NULL,NULL),(5,'SALE_DELIVERY','销售出库单','SD','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleDeliveryServiceImpl',0,NULL,NULL,'system','2026-10-03 14:07:17'),(6,'SALE_RETURN','销售退货单','SR','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleReturnServiceImpl',0,NULL,NULL,'system','2026-10-03 14:07:34'),(7,'RETAIL_OUT','零售出库单','LS','yyyyMMdd',4,1,1,0,'',1,1,'原 RetailOutSheetServiceImpl',0,NULL,NULL,NULL,NULL),(8,'RETAIL_RETURN','零售退货单','LT','yyyyMMdd',4,1,1,0,'',1,1,'原 RetailReturnServiceImpl',0,NULL,NULL,NULL,NULL),(9,'STOCK_CHECK','库存盘点单','SC','yyyyMMdd',4,1,1,3,'20261003',1,1,'原 StockCheckServiceImpl',0,NULL,NULL,'system','2026-10-03 19:33:45'),(10,'STOCK_TRANSFER','库存调拨单','ST','yyyyMMdd',4,1,1,0,'',1,1,'原 StockTransferServiceImpl',0,NULL,NULL,'system','2026-10-03 14:08:00'),(11,'STOCK_ADJUST','库存调整单','SA','yyyyMMdd',4,1,1,0,'',1,1,'原 StockAdjustSheetServiceImpl',0,NULL,NULL,NULL,NULL),(12,'SETTLE_SHEET','供应商结算单','JS','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleSheetServiceImpl',0,NULL,NULL,NULL,NULL),(13,'SETTLE_CHECK','供应商对账单','DZ','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleCheckSheetServiceImpl',0,NULL,NULL,NULL,NULL),(14,'SETTLE_FEE','供应商费用单','FY','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleFeeSheetServiceImpl',0,NULL,NULL,NULL,NULL),(15,'SETTLE_PRE','供应商预收预付单','YF','yyyyMMdd',4,1,1,0,'',1,1,'原 SettlePreSheetServiceImpl',0,NULL,NULL,NULL,NULL),(16,'CUSTOMER_SETTLE','客户结算单','KJS','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerSettleSheetServiceImpl',0,NULL,NULL,NULL,NULL),(17,'CUSTOMER_CHECK','客户对账单','KDZ','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerCheckSheetServiceImpl',0,NULL,NULL,NULL,NULL),(18,'CUSTOMER_FEE_IN','客户费用单(收入)','KSK','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerFeeSheetServiceImpl sheetType=1',0,NULL,NULL,NULL,NULL),(19,'CUSTOMER_FEE_OUT','客户费用单(支出)','KKK','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerFeeSheetServiceImpl sheetType!=1',0,NULL,NULL,NULL,NULL),(20,'CUSTOMER_PRE','客户预收预付单','KYS','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerPreSheetServiceImpl',0,NULL,NULL,NULL,NULL),(21,'SETTLE_IN_OUT_ITEM','收支项目','SZ','',4,1,1,0,'',0,1,'原 SettleInOutItemServiceImpl 无日期段',0,NULL,NULL,NULL,NULL),(22,'STOCK_ADJUST_REASON','库存调整原因','','',3,1,1,0,'',0,1,'原 StockAdjustReasonServiceImpl 3位纯流水',0,NULL,NULL,NULL,NULL),(23,'BASE_MATERIAL','物料','MAT','',6,1,1,86,'20261004',0,1,'原 BaseMaterialServiceImpl',0,NULL,NULL,'system','2026-10-03 12:30:06'),(24,'BASE_CUSTOMER','客户','CUS','',6,1,1,3,'',0,1,'原 BaseCustomerServiceImpl',0,NULL,NULL,'system','2026-10-03 12:47:42'),(25,'BASE_SUPPLIER','供应商','SUP','',6,1,1,16,'',0,1,'原 BaseSupplierServiceImpl',0,NULL,NULL,'system','2026-10-03 12:47:42'),(26,'BASE_WAREHOUSE','仓库','WH','',6,1,1,4,'',0,1,'原 BaseWarehouseServiceImpl',0,NULL,NULL,NULL,NULL),(27,'BASE_BRAND','品牌','BRD','',6,1,1,11,'',0,1,'原 BaseBrandServiceImpl',0,NULL,NULL,NULL,NULL),(28,'BASE_UNIT','计量单位','UNIT','',6,1,1,16,'',0,1,'原 BaseUnitServiceImpl',0,NULL,NULL,NULL,NULL),(29,'BASE_MATERIAL_CATEGORY','物料分类','MC','',6,1,1,9,'',0,1,'原 BaseMaterialCategoryServiceImpl',0,NULL,NULL,NULL,NULL),(59,'SPU','商品SPU','SPU','',6,1,1,28,'20261004',0,1,'商品中心SPU编号',0,NULL,'2026-10-03 14:31:35','system','2026-10-03 14:31:35'),(60,'SALE_PROPERTY','销售属性组','SP','',6,1,1,6,'20261004',0,1,'商品销售属性组编号',0,NULL,'2026-10-03 14:33:24','system','2026-10-03 14:33:24'),(61,'SALE_PROPERTY_ITEM','销售属性值','SI','',6,1,1,48,'20261004',0,1,'商品销售属性值编号',0,NULL,'2026-10-03 14:33:24','system','2026-10-03 14:33:24'),(62,'STOCK_TAKE_PLAN','库存盘点任务','PD','yyyyMMdd',4,1,1,6,'20261003',1,1,'库存盘点任务编号',0,NULL,'2026-10-03 15:06:18','system','2026-10-03 15:06:18'),(63,'STOCK_TAKE_PRE','预先盘点单','YPD','yyyyMMdd',4,1,1,2,'20261003',1,1,'预先盘点单编号',0,NULL,'2026-10-03 15:06:18','system','2026-10-03 15:06:18'),(64,'STOCK_COST_ADJUST','库存成本调整单','CB','yyyyMMdd',4,1,1,0,'',1,1,'库存成本调整单编号',0,NULL,'2026-10-03 15:06:18','system','2026-10-03 15:06:18');
/*!40000 ALTER TABLE `sys_code_rule` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-06 15:11:52
