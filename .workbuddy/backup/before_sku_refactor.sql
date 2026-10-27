mysqldump: [Warning] Using a password on the command line interface can be insecure.
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
-- Table structure for table `base_material`
--

DROP TABLE IF EXISTS `base_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_material` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物料ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `code` varchar(50) NOT NULL COMMENT '物料编号',
  `name` varchar(200) NOT NULL COMMENT '物料名称',
  `short_name` varchar(100) DEFAULT NULL COMMENT '物料简称',
  `specification` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `unit_id` bigint NOT NULL COMMENT '计量单位ID',
  `purchase_price` decimal(18,2) DEFAULT '0.00' COMMENT '采购价格',
  `sale_price` decimal(18,2) DEFAULT '0.00' COMMENT '销售价格',
  `retail_price` decimal(18,2) DEFAULT '0.00' COMMENT '零售价格',
  `min_stock` decimal(18,2) DEFAULT '0.00' COMMENT '最低库存',
  `max_stock` decimal(18,2) DEFAULT '0.00' COMMENT '最高库存',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记: 0-未删除 1-已删除',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_unit_id` (`unit_id`),
  KEY `idx_status` (`status`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_material`
--

LOCK TABLES `base_material` WRITE;
/*!40000 ALTER TABLE `base_material` DISABLE KEYS */;
INSERT INTO `base_material` VALUES (1,1,'MAT000001','钢板Q235','钢板','1200*2400*3mm',5,4500.00,5000.00,5500.00,100.00,1000.00,1,'普通碳素结构钢板',0,NULL,'2026-08-01 19:23:55',NULL,'2026-08-01 19:23:55'),(2,1,'MAT000002','铝合金6061','铝合金','1000*2000*2mm',5,28000.00,32000.00,35000.00,50.00,500.00,1,'铝镁硅合金',0,NULL,'2026-08-01 19:23:55',NULL,'2026-08-01 19:23:55'),(3,4,'MAT000003','螺栓M8*20','螺栓','M8*20 304不锈钢',1,0.50,0.80,1.00,1000.00,10000.00,1,'不锈钢螺栓',0,NULL,'2026-08-01 19:23:55',NULL,'2026-08-01 19:23:55'),(4,5,'MAT000004','纸箱','纸箱','500*400*300mm',1,5.00,8.00,10.00,100.00,1000.00,1,'五层瓦楞纸箱',0,NULL,'2026-08-01 19:23:55',NULL,'2026-08-01 19:23:55'),(28,1,'MAT000005','日志测试物料',NULL,NULL,5,0.00,0.00,0.00,0.00,0.00,1,NULL,1,'system','2026-10-03 13:40:18','system','2026-10-03 13:40:47');
/*!40000 ALTER TABLE `base_material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `base_unit`
--

DROP TABLE IF EXISTS `base_unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_unit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '单位ID',
  `code` varchar(50) NOT NULL COMMENT '单位编号',
  `name` varchar(50) NOT NULL COMMENT '单位名称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记: 0-未删除 1-已删除',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计量单位表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_unit`
--

LOCK TABLES `base_unit` WRITE;
/*!40000 ALTER TABLE `base_unit` DISABLE KEYS */;
INSERT INTO `base_unit` VALUES (1,'UNIT000001','个',1,1,'通用计数单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(2,'UNIT000002','台',2,1,'设备类单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(3,'UNIT000003','件',3,1,'通用计件单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(4,'UNIT000004','套',4,1,'成套产品单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(5,'UNIT000005','千克',5,1,'重量单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(6,'UNIT000006','克',6,1,'重量单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(7,'UNIT000007','吨',7,1,'重量单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(8,'UNIT000008','米',8,1,'长度单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(9,'UNIT000009','厘米',9,1,'长度单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(10,'UNIT000010','平方米',10,1,'面积单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(11,'UNIT000011','立方米',11,1,'体积单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(12,'UNIT000012','升',12,1,'容量单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(13,'UNIT000013','箱',13,1,'包装单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(14,'UNIT000014','包',14,1,'包装单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25'),(15,'UNIT000015','袋',15,1,'包装单位',0,NULL,'2026-08-01 19:24:25',NULL,'2026-08-01 19:24:25');
/*!40000 ALTER TABLE `base_unit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `base_material_category`
--

DROP TABLE IF EXISTS `base_material_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_material_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID(0表示顶级分类)',
  `code` varchar(50) NOT NULL COMMENT '分类编号',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记: 0-未删除 1-已删除',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_material_category`
--

LOCK TABLES `base_material_category` WRITE;
/*!40000 ALTER TABLE `base_material_category` DISABLE KEYS */;
INSERT INTO `base_material_category` VALUES (1,0,'MC000001','原材料',1,1,'生产用原材料',0,NULL,'2026-08-01 19:24:04',NULL,'2026-08-01 19:24:04'),(2,0,'MC000002','半成品',2,1,'生产过程中的半成品',0,NULL,'2026-08-01 19:24:04',NULL,'2026-08-01 19:24:04'),(3,0,'MC000003','成品',3,1,'最终产品',0,NULL,'2026-08-01 19:24:04',NULL,'2026-08-01 19:24:04'),(4,0,'MC000004','辅料',4,1,'生产辅助材料',0,NULL,'2026-08-01 19:24:04',NULL,'2026-08-01 19:24:04'),(5,0,'MC000005','包装物',5,1,'包装用材料',0,NULL,'2026-08-01 19:24:04',NULL,'2026-08-01 19:24:04');
/*!40000 ALTER TABLE `base_material_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_stock`
--

DROP TABLE IF EXISTS `product_stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `sc_id` bigint NOT NULL COMMENT '仓库ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `stock_num` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT '库存数量',
  `tax_price` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT '含税价格',
  `tax_amount` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT '含税金额',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sc_product` (`sc_id`,`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_stock`
--

LOCK TABLES `product_stock` WRITE;
/*!40000 ALTER TABLE `product_stock` DISABLE KEYS */;
INSERT INTO `product_stock` VALUES (1,2,1,90.0000,110.0000,9900.0000,'system','2026-10-01 22:44:47','system','2026-10-01 22:44:47',0);
/*!40000 ALTER TABLE `product_stock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_stock_log`
--

DROP TABLE IF EXISTS `product_stock_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_stock_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `sc_id` bigint NOT NULL COMMENT '仓库ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `ori_stock_num` decimal(16,4) NOT NULL COMMENT '原库存数量',
  `cur_stock_num` decimal(16,4) NOT NULL COMMENT '现库存数量',
  `stock_num` decimal(16,4) NOT NULL COMMENT '变动库存数量',
  `biz_id` bigint DEFAULT NULL COMMENT '业务单据ID',
  `biz_code` varchar(64) DEFAULT NULL COMMENT '业务单据号',
  `biz_type` int NOT NULL COMMENT '业务类型',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sc_product` (`sc_id`,`product_id`),
  KEY `idx_biz` (`biz_id`,`biz_type`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品库存日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_stock_log`
--

LOCK TABLES `product_stock_log` WRITE;
/*!40000 ALTER TABLE `product_stock_log` DISABLE KEYS */;
INSERT INTO `product_stock_log` VALUES (1,2,1,0.0000,100.0000,100.0000,1,'PR202610010001',1,'system','2026-10-01 22:44:47'),(2,2,1,100.0000,90.0000,10.0000,1,'PTH202610030001',2,'system','2026-10-03 13:50:12');
/*!40000 ALTER TABLE `product_stock_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_order_detail`
--

DROP TABLE IF EXISTS `purchase_order_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `order_num` decimal(16,4) NOT NULL COMMENT '采购数量',
  `tax_price` decimal(16,4) NOT NULL COMMENT '含税单价',
  `tax_amount` decimal(16,4) NOT NULL COMMENT '含税金额',
  `received_num` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT '已收货数量',
  `description` varchar(500) DEFAULT NULL COMMENT '备注',
  `sort` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order_detail`
--

LOCK TABLES `purchase_order_detail` WRITE;
/*!40000 ALTER TABLE `purchase_order_detail` DISABLE KEYS */;
INSERT INTO `purchase_order_detail` VALUES (1,1,1,100.0000,110.0000,11000.0000,100.0000,'',0);
/*!40000 ALTER TABLE `purchase_order_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_receipt_detail`
--

DROP TABLE IF EXISTS `purchase_receipt_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_receipt_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `receipt_id` bigint NOT NULL COMMENT '收货单ID',
  `order_detail_id` bigint NOT NULL COMMENT '订单明细ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `order_num` decimal(16,4) NOT NULL COMMENT '订单数量',
  `receive_num` decimal(16,4) NOT NULL COMMENT '本次收货数量',
  `tax_price` decimal(16,4) NOT NULL COMMENT '含税单价',
  `tax_amount` decimal(16,4) NOT NULL COMMENT '含税金额',
  `description` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_receipt` (`receipt_id`),
  KEY `idx_order_detail` (`order_detail_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购收货明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_receipt_detail`
--

LOCK TABLES `purchase_receipt_detail` WRITE;
/*!40000 ALTER TABLE `purchase_receipt_detail` DISABLE KEYS */;
INSERT INTO `purchase_receipt_detail` VALUES (1,1,1,1,100.0000,100.0000,110.0000,11000.0000,'');
/*!40000 ALTER TABLE `purchase_receipt_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_return_detail`
--

DROP TABLE IF EXISTS `purchase_return_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_return_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `return_id` bigint NOT NULL COMMENT '退货单ID',
  `receipt_detail_id` bigint DEFAULT NULL COMMENT '收货明细ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `receive_num` decimal(16,2) DEFAULT '0.00' COMMENT '原收货数量',
  `return_num` decimal(16,2) DEFAULT '0.00' COMMENT '退货数量',
  `tax_price` decimal(16,2) DEFAULT '0.00' COMMENT '含税单价',
  `tax_amount` decimal(16,2) DEFAULT '0.00' COMMENT '含税金额',
  `description` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_return_id` (`return_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购退货明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_return_detail`
--

LOCK TABLES `purchase_return_detail` WRITE;
/*!40000 ALTER TABLE `purchase_return_detail` DISABLE KEYS */;
INSERT INTO `purchase_return_detail` VALUES (1,1,1,1,100.00,10.00,110.00,1100.00,'有毛刺');
/*!40000 ALTER TABLE `purchase_return_detail` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单据编号规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_code_rule`
--

LOCK TABLES `sys_code_rule` WRITE;
/*!40000 ALTER TABLE `sys_code_rule` DISABLE KEYS */;
INSERT INTO `sys_code_rule` VALUES (1,'PURCHASE_ORDER','采购订单','PO','yyyyMMdd',4,1,1,1,'20261001',1,1,'原 PurchaseOrderServiceImpl',0,NULL,NULL,'system','2026-10-03 12:30:39'),(2,'PURCHASE_RECEIPT','采购收货单','PR','yyyyMMdd',4,1,1,1,'20261001',1,1,'原 PurchaseReceiptServiceImpl',0,NULL,NULL,NULL,NULL),(3,'PURCHASE_RETURN','采购退货单','PTH','yyyyMMdd',4,1,1,1,'20261003',1,1,'原 PurchaseReturnServiceImpl',0,NULL,NULL,'system','2026-10-03 13:49:45'),(4,'SALE_ORDER','销售订单','SO','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleOrderServiceImpl',0,NULL,NULL,NULL,NULL),(5,'SALE_DELIVERY','销售出库单','SD','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleDeliveryServiceImpl',0,NULL,NULL,'system','2026-10-03 14:07:17'),(6,'SALE_RETURN','销售退货单','SR','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleReturnServiceImpl',0,NULL,NULL,'system','2026-10-03 14:07:34'),(7,'RETAIL_OUT','零售出库单','LS','yyyyMMdd',4,1,1,0,'',1,1,'原 RetailOutSheetServiceImpl',0,NULL,NULL,NULL,NULL),(8,'RETAIL_RETURN','零售退货单','LT','yyyyMMdd',4,1,1,0,'',1,1,'原 RetailReturnServiceImpl',0,NULL,NULL,NULL,NULL),(9,'STOCK_CHECK','库存盘点单','SC','yyyyMMdd',4,1,1,0,'',1,1,'原 StockCheckServiceImpl',0,NULL,NULL,NULL,NULL),(10,'STOCK_TRANSFER','库存调拨单','ST','yyyyMMdd',4,1,1,0,'',1,1,'原 StockTransferServiceImpl',0,NULL,NULL,'system','2026-10-03 14:08:00'),(11,'STOCK_ADJUST','库存调整单','SA','yyyyMMdd',4,1,1,0,'',1,1,'原 StockAdjustSheetServiceImpl',0,NULL,NULL,NULL,NULL),(12,'SETTLE_SHEET','供应商结算单','JS','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleSheetServiceImpl',0,NULL,NULL,NULL,NULL),(13,'SETTLE_CHECK','供应商对账单','DZ','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleCheckSheetServiceImpl',0,NULL,NULL,NULL,NULL),(14,'SETTLE_FEE','供应商费用单','FY','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleFeeSheetServiceImpl',0,NULL,NULL,NULL,NULL),(15,'SETTLE_PRE','供应商预收预付单','YF','yyyyMMdd',4,1,1,0,'',1,1,'原 SettlePreSheetServiceImpl',0,NULL,NULL,NULL,NULL),(16,'CUSTOMER_SETTLE','客户结算单','KJS','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerSettleSheetServiceImpl',0,NULL,NULL,NULL,NULL),(17,'CUSTOMER_CHECK','客户对账单','KDZ','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerCheckSheetServiceImpl',0,NULL,NULL,NULL,NULL),(18,'CUSTOMER_FEE_IN','客户费用单(收入)','KSK','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerFeeSheetServiceImpl sheetType=1',0,NULL,NULL,NULL,NULL),(19,'CUSTOMER_FEE_OUT','客户费用单(支出)','KKK','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerFeeSheetServiceImpl sheetType!=1',0,NULL,NULL,NULL,NULL),(20,'CUSTOMER_PRE','客户预收预付单','KYS','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerPreSheetServiceImpl',0,NULL,NULL,NULL,NULL),(21,'SETTLE_IN_OUT_ITEM','收支项目','SZ','',4,1,1,0,'',0,1,'原 SettleInOutItemServiceImpl 无日期段',0,NULL,NULL,NULL,NULL),(22,'STOCK_ADJUST_REASON','库存调整原因','','',3,1,1,0,'',0,1,'原 StockAdjustReasonServiceImpl 3位纯流水',0,NULL,NULL,NULL,NULL),(23,'BASE_MATERIAL','物料','MAT','',6,1,1,4,'20261003',0,1,'原 BaseMaterialServiceImpl',0,NULL,NULL,'system','2026-10-03 12:30:06'),(24,'BASE_CUSTOMER','客户','CUS','',6,1,1,3,'',0,1,'原 BaseCustomerServiceImpl',0,NULL,NULL,'system','2026-10-03 12:47:42'),(25,'BASE_SUPPLIER','供应商','SUP','',6,1,1,3,'',0,1,'原 BaseSupplierServiceImpl',0,NULL,NULL,'system','2026-10-03 12:47:42'),(26,'BASE_WAREHOUSE','仓库','WH','',6,1,1,4,'',0,1,'原 BaseWarehouseServiceImpl',0,NULL,NULL,NULL,NULL),(27,'BASE_BRAND','品牌','BRD','',6,1,1,3,'',0,1,'原 BaseBrandServiceImpl',0,NULL,NULL,NULL,NULL),(28,'BASE_UNIT','计量单位','UNIT','',6,1,1,15,'',0,1,'原 BaseUnitServiceImpl',0,NULL,NULL,NULL,NULL),(29,'BASE_MATERIAL_CATEGORY','物料分类','MC','',6,1,1,5,'',0,1,'原 BaseMaterialCategoryServiceImpl',0,NULL,NULL,NULL,NULL);
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

-- Dump completed on 2026-10-03 14:31:00
