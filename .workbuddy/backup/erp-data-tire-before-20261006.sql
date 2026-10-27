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
-- Table structure for table `base_brand`
--

DROP TABLE IF EXISTS `base_brand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_brand` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(50) NOT NULL COMMENT '品牌编号',
  `name` varchar(100) NOT NULL COMMENT '品牌名称',
  `short_name` varchar(50) DEFAULT NULL COMMENT '品牌简称',
  `logo` varchar(200) DEFAULT NULL COMMENT '品牌Logo',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `sort` int DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='品牌信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_brand`
--

LOCK TABLES `base_brand` WRITE;
/*!40000 ALTER TABLE `base_brand` DISABLE KEYS */;
INSERT INTO `base_brand` VALUES (1,'BRD000001','苹果','Apple',NULL,1,1,'电子产品品牌','2026-08-01 19:23:34','2026-08-01 19:23:34',NULL,NULL,0),(2,'BRD000002','华为','Huawei',NULL,1,2,'国产之光','2026-08-01 19:23:34','2026-08-01 19:23:34',NULL,NULL,0),(3,'BRD000003','小米','Xiaomi',NULL,1,3,'性价比之王','2026-08-01 19:23:34','2026-08-01 19:23:34',NULL,NULL,0);
/*!40000 ALTER TABLE `base_brand` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `base_material`
--

DROP TABLE IF EXISTS `base_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_material` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物料ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `spu_id` bigint DEFAULT NULL COMMENT 'SPU ID',
  `code` varchar(50) NOT NULL COMMENT '物料编号',
  `name` varchar(200) NOT NULL COMMENT '物料名称',
  `short_name` varchar(100) DEFAULT NULL COMMENT '物料简称',
  `specification` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `sale_property_text` varchar(500) DEFAULT '' COMMENT '销售属性文本(如:颜色:红 / 尺码:XL)',
  `main_image` varchar(500) DEFAULT NULL COMMENT 'SKU图片',
  `is_multi_code` tinyint DEFAULT '0' COMMENT '是否一品多码(0=否,1=是)',
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
  KEY `idx_name` (`name`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_material`
--

LOCK TABLES `base_material` WRITE;
/*!40000 ALTER TABLE `base_material` DISABLE KEYS */;
INSERT INTO `base_material` VALUES (1,1,1,'MAT000001','钢板Q235','钢板','1200*2400*3mm','',NULL,0,5,4500.00,5000.00,5500.00,100.00,1000.00,1,'普通碳素结构钢板',1,NULL,'2026-08-01 19:23:55','system','2026-10-04 16:26:13'),(2,1,2,'MAT000002','铝合金6061','铝合金','1000*2000*2mm','',NULL,0,5,28000.00,32000.00,35000.00,50.00,500.00,1,'铝镁硅合金',1,NULL,'2026-08-01 19:23:55','system','2026-10-04 16:26:16'),(3,4,3,'MAT000003','螺栓M8*20','螺栓','M8*20 304不锈钢','',NULL,0,1,0.50,0.80,1.00,1000.00,10000.00,1,'不锈钢螺栓',1,NULL,'2026-08-01 19:23:55','system','2026-10-04 16:26:18'),(4,5,4,'MAT000004','纸箱','纸箱','500*400*300mm','',NULL,0,1,5.00,8.00,10.00,100.00,1000.00,1,'五层瓦楞纸箱',1,NULL,'2026-08-01 19:23:55','system','2026-10-04 16:26:20'),(42,3,11,'MAT000012','纯棉T恤 颜色红 尺码S','T恤','纯棉 200g','颜色:红 / 尺码:S',NULL,0,3,0.00,0.00,0.00,0.00,0.00,1,NULL,0,'system','2026-10-04 16:25:26','system','2026-10-04 16:25:26'),(43,3,11,'MAT000013','纯棉T恤 颜色红 尺码M','T恤','纯棉 200g','颜色:红 / 尺码:M',NULL,0,3,0.00,0.00,0.00,0.00,0.00,1,NULL,0,'system','2026-10-04 16:25:26','system','2026-10-04 16:25:26'),(44,3,11,'MAT000014','纯棉T恤 颜色红 尺码L','T恤','纯棉 200g','颜色:红 / 尺码:L',NULL,0,3,0.00,0.00,0.00,0.00,0.00,1,NULL,0,'system','2026-10-04 16:25:26','system','2026-10-04 16:25:26'),(45,3,11,'MAT000015','纯棉T恤 颜色黑 尺码S','T恤','纯棉 200g','颜色:黑 / 尺码:S',NULL,0,3,0.00,0.00,0.00,0.00,0.00,1,NULL,0,'system','2026-10-04 16:25:26','system','2026-10-04 16:25:26'),(46,3,11,'MAT000016','纯棉T恤 颜色黑 尺码M','T恤','纯棉 200g','颜色:黑 / 尺码:M',NULL,0,3,0.00,0.00,0.00,0.00,0.00,1,NULL,0,'system','2026-10-04 16:25:26','system','2026-10-04 16:25:26'),(47,3,11,'MAT000017','纯棉T恤 颜色黑 尺码L','T恤','纯棉 200g','颜色:黑 / 尺码:L',NULL,0,3,0.00,0.00,0.00,0.00,0.00,1,NULL,0,'system','2026-10-04 16:25:28','system','2026-10-04 16:25:28'),(48,3,11,'MAT000018','纯棉T恤 颜色蓝 尺码S','T恤','纯棉 200g','颜色:蓝 / 尺码:S',NULL,0,3,0.00,0.00,0.00,0.00,0.00,1,NULL,0,'system','2026-10-04 16:25:28','system','2026-10-04 16:25:28'),(49,3,11,'MAT000019','纯棉T恤 颜色蓝 尺码M','T恤','纯棉 200g','颜色:蓝 / 尺码:M',NULL,0,3,0.00,0.00,0.00,0.00,0.00,1,NULL,0,'system','2026-10-04 16:25:28','system','2026-10-04 16:25:28'),(50,3,11,'MAT000020','纯棉T恤 颜色蓝 尺码L','T恤','纯棉 200g','颜色:蓝 / 尺码:L',NULL,0,3,0.00,0.00,0.00,0.00,0.00,1,NULL,0,'system','2026-10-04 16:25:28','system','2026-10-04 16:25:28'),(51,3,11,'MAT000021','纯棉T恤 尺码S 颜色紫','T恤','纯棉 200g','尺码:S / 颜色:紫',NULL,0,3,15.00,20.00,25.00,100.00,10000.00,1,NULL,0,'system','2026-10-04 16:27:17','system','2026-10-04 16:29:45'),(52,3,11,'MAT000022','纯棉T恤 尺码M 颜色紫','T恤','纯棉 200g','尺码:M / 颜色:紫',NULL,0,3,0.00,0.00,0.00,0.00,0.00,1,NULL,0,'system','2026-10-04 16:27:17','system','2026-10-04 16:27:17'),(53,3,11,'MAT000023','纯棉T恤 尺码L 颜色紫','T恤','纯棉 200g','尺码:L / 颜色:紫',NULL,0,3,0.00,0.00,0.00,0.00,0.00,1,NULL,0,'system','2026-10-04 16:27:17','system','2026-10-04 16:27:17');
/*!40000 ALTER TABLE `base_material` ENABLE KEYS */;
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
-- Table structure for table `base_spu`
--

DROP TABLE IF EXISTS `base_spu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_spu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `brand_id` bigint DEFAULT NULL COMMENT '品牌ID',
  `unit_id` bigint NOT NULL COMMENT '基本计量单位ID',
  `code` varchar(50) NOT NULL COMMENT 'SPU编号',
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `short_name` varchar(100) DEFAULT NULL COMMENT '商品简称',
  `specification` varchar(200) DEFAULT NULL COMMENT '主体规格描述',
  `main_image` varchar(500) DEFAULT NULL COMMENT '商品主图',
  `status` tinyint DEFAULT '1' COMMENT '状态(0=禁用,1=启用)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记(0=未删除,1=已删除)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SPU商品主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_spu`
--

LOCK TABLES `base_spu` WRITE;
/*!40000 ALTER TABLE `base_spu` DISABLE KEYS */;
INSERT INTO `base_spu` VALUES (1,1,NULL,5,'SPU000001','钢板Q235','钢板','1200*2400*3mm',NULL,1,'普通碳素结构钢板',1,NULL,'2026-10-03 14:31:35','system','2026-10-04 16:28:46'),(2,1,NULL,5,'SPU000002','铝合金6061','铝合金','1000*2000*2mm',NULL,1,'铝镁硅合金',1,NULL,'2026-10-03 14:31:35','system','2026-10-04 16:28:44'),(3,4,NULL,1,'SPU000003','螺栓M8*20','螺栓','M8*20 304不锈钢',NULL,1,'不锈钢螺栓',1,NULL,'2026-10-03 14:31:35','system','2026-10-04 16:28:42'),(4,5,NULL,1,'SPU000004','纸箱','纸箱','500*400*300mm',NULL,1,'五层瓦楞纸箱',0,NULL,'2026-10-03 14:31:35',NULL,'2026-10-03 14:31:35'),(11,3,NULL,3,'SPU000006','纯棉T恤','T恤','纯棉 200g',NULL,1,NULL,0,'system','2026-10-04 16:25:26','system','2026-10-04 16:27:17');
/*!40000 ALTER TABLE `base_spu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `base_sale_property`
--

DROP TABLE IF EXISTS `base_sale_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_sale_property` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '属性组ID',
  `code` varchar(50) NOT NULL COMMENT '属性组编码',
  `name` varchar(50) NOT NULL COMMENT '属性组名称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(0=禁用,1=启用)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记(0=未删除,1=已删除)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品销售属性组';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_sale_property`
--

LOCK TABLES `base_sale_property` WRITE;
/*!40000 ALTER TABLE `base_sale_property` DISABLE KEYS */;
INSERT INTO `base_sale_property` VALUES (9,'SP000003','颜色',0,1,NULL,0,'system','2026-10-04 16:23:52','system','2026-10-04 16:26:49'),(10,'SP000004','尺码',0,1,NULL,0,'system','2026-10-04 16:24:26','system','2026-10-04 16:24:26');
/*!40000 ALTER TABLE `base_sale_property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `base_sale_property_item`
--

DROP TABLE IF EXISTS `base_sale_property_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_sale_property_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '属性值ID',
  `property_id` bigint NOT NULL COMMENT '属性组ID',
  `code` varchar(50) NOT NULL COMMENT '属性值编码',
  `name` varchar(50) NOT NULL COMMENT '属性值名称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(0=禁用,1=启用)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记(0=未删除,1=已删除)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prop_code` (`property_id`,`code`),
  KEY `idx_property_id` (`property_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品销售属性值';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_sale_property_item`
--

LOCK TABLES `base_sale_property_item` WRITE;
/*!40000 ALTER TABLE `base_sale_property_item` DISABLE KEYS */;
INSERT INTO `base_sale_property_item` VALUES (19,9,'SI000006','红',0,1,NULL,0,NULL,'2026-10-04 16:23:52',NULL,'2026-10-04 16:23:52'),(20,9,'SI000007','黑',1,1,NULL,0,NULL,'2026-10-04 16:23:52',NULL,'2026-10-04 16:23:52'),(21,9,'SI000008','蓝',2,1,NULL,0,NULL,'2026-10-04 16:23:52',NULL,'2026-10-04 16:23:52'),(22,10,'SI000009','S',0,1,NULL,0,NULL,'2026-10-04 16:24:25',NULL,'2026-10-04 16:24:25'),(23,10,'SI000010','M',1,1,NULL,0,NULL,'2026-10-04 16:24:25',NULL,'2026-10-04 16:24:25'),(24,10,'SI000011','L',2,1,NULL,0,NULL,'2026-10-04 16:24:25',NULL,'2026-10-04 16:24:25'),(25,9,'SI000012','紫',3,1,NULL,0,NULL,'2026-10-04 16:26:48',NULL,'2026-10-04 16:26:48');
/*!40000 ALTER TABLE `base_sale_property_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `base_spu_sale_property`
--

DROP TABLE IF EXISTS `base_spu_sale_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_spu_sale_property` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `spu_id` bigint NOT NULL COMMENT 'SPU ID',
  `property_id` bigint NOT NULL COMMENT '属性组ID',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记(0=未删除,1=已删除)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_property` (`spu_id`,`property_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SPU销售属性绑定';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_spu_sale_property`
--

LOCK TABLES `base_spu_sale_property` WRITE;
/*!40000 ALTER TABLE `base_spu_sale_property` DISABLE KEYS */;
INSERT INTO `base_spu_sale_property` VALUES (5,9,5,0,NULL,'2026-10-03 14:42:16',NULL,'2026-10-03 14:42:16'),(6,9,6,0,NULL,'2026-10-03 14:42:16',NULL,'2026-10-03 14:42:16'),(9,10,7,0,NULL,'2026-10-03 14:50:17',NULL,'2026-10-03 14:50:17'),(10,10,8,0,NULL,'2026-10-03 14:50:17',NULL,'2026-10-03 14:50:17'),(13,11,9,0,NULL,'2026-10-04 16:27:16',NULL,'2026-10-04 16:27:16'),(14,11,10,0,NULL,'2026-10-04 16:27:16',NULL,'2026-10-04 16:27:16');
/*!40000 ALTER TABLE `base_spu_sale_property` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `base_material_property_rel`
--

DROP TABLE IF EXISTS `base_material_property_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_material_property_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `material_id` bigint NOT NULL COMMENT 'SKU ID(=base_material.id)',
  `property_id` bigint NOT NULL COMMENT '属性组ID',
  `property_item_id` bigint NOT NULL COMMENT '属性值ID',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记(0=未删除,1=已删除)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mat_property` (`material_id`,`property_id`),
  KEY `idx_item` (`property_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SKU销售属性关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_material_property_rel`
--

LOCK TABLES `base_material_property_rel` WRITE;
/*!40000 ALTER TABLE `base_material_property_rel` DISABLE KEYS */;
INSERT INTO `base_material_property_rel` VALUES (25,42,9,19,0,NULL,'2026-10-04 16:25:25',NULL,'2026-10-04 16:25:25'),(26,42,10,22,0,NULL,'2026-10-04 16:25:25',NULL,'2026-10-04 16:25:25'),(27,43,9,19,0,NULL,'2026-10-04 16:25:25',NULL,'2026-10-04 16:25:25'),(28,43,10,23,0,NULL,'2026-10-04 16:25:25',NULL,'2026-10-04 16:25:25'),(29,44,9,19,0,NULL,'2026-10-04 16:25:25',NULL,'2026-10-04 16:25:25'),(30,44,10,24,0,NULL,'2026-10-04 16:25:26',NULL,'2026-10-04 16:25:26'),(31,45,9,20,0,NULL,'2026-10-04 16:25:26',NULL,'2026-10-04 16:25:26'),(32,45,10,22,0,NULL,'2026-10-04 16:25:26',NULL,'2026-10-04 16:25:26'),(33,46,9,20,0,NULL,'2026-10-04 16:25:26',NULL,'2026-10-04 16:25:26'),(34,46,10,23,0,NULL,'2026-10-04 16:25:26',NULL,'2026-10-04 16:25:26'),(35,47,9,20,0,NULL,'2026-10-04 16:25:27',NULL,'2026-10-04 16:25:27'),(36,47,10,24,0,NULL,'2026-10-04 16:25:27',NULL,'2026-10-04 16:25:27'),(37,48,9,21,0,NULL,'2026-10-04 16:25:27',NULL,'2026-10-04 16:25:27'),(38,48,10,22,0,NULL,'2026-10-04 16:25:27',NULL,'2026-10-04 16:25:27'),(39,49,9,21,0,NULL,'2026-10-04 16:25:27',NULL,'2026-10-04 16:25:27'),(40,49,10,23,0,NULL,'2026-10-04 16:25:27',NULL,'2026-10-04 16:25:27'),(41,50,9,21,0,NULL,'2026-10-04 16:25:27',NULL,'2026-10-04 16:25:27'),(42,50,10,24,0,NULL,'2026-10-04 16:25:27',NULL,'2026-10-04 16:25:27'),(43,51,9,25,0,NULL,'2026-10-04 16:27:16',NULL,'2026-10-04 16:27:16'),(44,51,10,22,0,NULL,'2026-10-04 16:27:16',NULL,'2026-10-04 16:27:16'),(45,52,9,25,0,NULL,'2026-10-04 16:27:16',NULL,'2026-10-04 16:27:16'),(46,52,10,23,0,NULL,'2026-10-04 16:27:16',NULL,'2026-10-04 16:27:16'),(47,53,9,25,0,NULL,'2026-10-04 16:27:16',NULL,'2026-10-04 16:27:16'),(48,53,10,24,0,NULL,'2026-10-04 16:27:16',NULL,'2026-10-04 16:27:16');
/*!40000 ALTER TABLE `base_material_property_rel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `base_material_code`
--

DROP TABLE IF EXISTS `base_material_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `base_material_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `material_id` bigint NOT NULL COMMENT 'SKU ID',
  `code` varchar(50) NOT NULL COMMENT '编码/条码',
  `code_type` tinyint NOT NULL DEFAULT '1' COMMENT '类型(1=主编号,2=条码,3=其他)',
  `is_main` tinyint DEFAULT '0' COMMENT '是否主码(0=否,1=是)',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记(0=未删除,1=已删除)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_type` (`code`,`code_type`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SKU一品多码';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_material_code`
--

LOCK TABLES `base_material_code` WRITE;
/*!40000 ALTER TABLE `base_material_code` DISABLE KEYS */;
INSERT INTO `base_material_code` VALUES (1,1,'MAT000001',1,1,'系统迁移生成',0,NULL,'2026-10-03 14:31:35',NULL,'2026-10-03 14:31:35'),(2,2,'MAT000002',1,1,'系统迁移生成',0,NULL,'2026-10-03 14:31:35',NULL,'2026-10-03 14:31:35'),(3,3,'MAT000003',1,1,'系统迁移生成',0,NULL,'2026-10-03 14:31:35',NULL,'2026-10-03 14:31:35'),(4,4,'MAT000004',1,1,'系统迁移生成',0,NULL,'2026-10-03 14:31:35',NULL,'2026-10-03 14:31:35');
/*!40000 ALTER TABLE `base_material_code` ENABLE KEYS */;
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
INSERT INTO `sys_code_rule` VALUES (1,'PURCHASE_ORDER','采购订单','PO','yyyyMMdd',4,1,1,1,'20261001',1,1,'原 PurchaseOrderServiceImpl',0,NULL,NULL,'system','2026-10-03 12:30:39'),(2,'PURCHASE_RECEIPT','采购收货单','PR','yyyyMMdd',4,1,1,1,'20261001',1,1,'原 PurchaseReceiptServiceImpl',0,NULL,NULL,NULL,NULL),(3,'PURCHASE_RETURN','采购退货单','PTH','yyyyMMdd',4,1,1,1,'20261003',1,1,'原 PurchaseReturnServiceImpl',0,NULL,NULL,'system','2026-10-03 13:49:45'),(4,'SALE_ORDER','销售订单','SO','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleOrderServiceImpl',0,NULL,NULL,NULL,NULL),(5,'SALE_DELIVERY','销售出库单','SD','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleDeliveryServiceImpl',0,NULL,NULL,'system','2026-10-03 14:07:17'),(6,'SALE_RETURN','销售退货单','SR','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleReturnServiceImpl',0,NULL,NULL,'system','2026-10-03 14:07:34'),(7,'RETAIL_OUT','零售出库单','LS','yyyyMMdd',4,1,1,0,'',1,1,'原 RetailOutSheetServiceImpl',0,NULL,NULL,NULL,NULL),(8,'RETAIL_RETURN','零售退货单','LT','yyyyMMdd',4,1,1,0,'',1,1,'原 RetailReturnServiceImpl',0,NULL,NULL,NULL,NULL),(9,'STOCK_CHECK','库存盘点单','SC','yyyyMMdd',4,1,1,3,'20261003',1,1,'原 StockCheckServiceImpl',0,NULL,NULL,'system','2026-10-03 19:33:45'),(10,'STOCK_TRANSFER','库存调拨单','ST','yyyyMMdd',4,1,1,0,'',1,1,'原 StockTransferServiceImpl',0,NULL,NULL,'system','2026-10-03 14:08:00'),(11,'STOCK_ADJUST','库存调整单','SA','yyyyMMdd',4,1,1,0,'',1,1,'原 StockAdjustSheetServiceImpl',0,NULL,NULL,NULL,NULL),(12,'SETTLE_SHEET','供应商结算单','JS','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleSheetServiceImpl',0,NULL,NULL,NULL,NULL),(13,'SETTLE_CHECK','供应商对账单','DZ','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleCheckSheetServiceImpl',0,NULL,NULL,NULL,NULL),(14,'SETTLE_FEE','供应商费用单','FY','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleFeeSheetServiceImpl',0,NULL,NULL,NULL,NULL),(15,'SETTLE_PRE','供应商预收预付单','YF','yyyyMMdd',4,1,1,0,'',1,1,'原 SettlePreSheetServiceImpl',0,NULL,NULL,NULL,NULL),(16,'CUSTOMER_SETTLE','客户结算单','KJS','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerSettleSheetServiceImpl',0,NULL,NULL,NULL,NULL),(17,'CUSTOMER_CHECK','客户对账单','KDZ','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerCheckSheetServiceImpl',0,NULL,NULL,NULL,NULL),(18,'CUSTOMER_FEE_IN','客户费用单(收入)','KSK','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerFeeSheetServiceImpl sheetType=1',0,NULL,NULL,NULL,NULL),(19,'CUSTOMER_FEE_OUT','客户费用单(支出)','KKK','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerFeeSheetServiceImpl sheetType!=1',0,NULL,NULL,NULL,NULL),(20,'CUSTOMER_PRE','客户预收预付单','KYS','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerPreSheetServiceImpl',0,NULL,NULL,NULL,NULL),(21,'SETTLE_IN_OUT_ITEM','收支项目','SZ','',4,1,1,0,'',0,1,'原 SettleInOutItemServiceImpl 无日期段',0,NULL,NULL,NULL,NULL),(22,'STOCK_ADJUST_REASON','库存调整原因','','',3,1,1,0,'',0,1,'原 StockAdjustReasonServiceImpl 3位纯流水',0,NULL,NULL,NULL,NULL),(23,'BASE_MATERIAL','物料','MAT','',6,1,1,23,'20261004',0,1,'原 BaseMaterialServiceImpl',0,NULL,NULL,'system','2026-10-03 12:30:06'),(24,'BASE_CUSTOMER','客户','CUS','',6,1,1,3,'',0,1,'原 BaseCustomerServiceImpl',0,NULL,NULL,'system','2026-10-03 12:47:42'),(25,'BASE_SUPPLIER','供应商','SUP','',6,1,1,3,'',0,1,'原 BaseSupplierServiceImpl',0,NULL,NULL,'system','2026-10-03 12:47:42'),(26,'BASE_WAREHOUSE','仓库','WH','',6,1,1,4,'',0,1,'原 BaseWarehouseServiceImpl',0,NULL,NULL,NULL,NULL),(27,'BASE_BRAND','品牌','BRD','',6,1,1,3,'',0,1,'原 BaseBrandServiceImpl',0,NULL,NULL,NULL,NULL),(28,'BASE_UNIT','计量单位','UNIT','',6,1,1,15,'',0,1,'原 BaseUnitServiceImpl',0,NULL,NULL,NULL,NULL),(29,'BASE_MATERIAL_CATEGORY','物料分类','MC','',6,1,1,5,'',0,1,'原 BaseMaterialCategoryServiceImpl',0,NULL,NULL,NULL,NULL),(59,'SPU','商品SPU','SPU','',6,1,1,6,'20261004',0,1,'商品中心SPU编号',0,NULL,'2026-10-03 14:31:35','system','2026-10-03 14:31:35'),(60,'SALE_PROPERTY','销售属性组','SP','',6,1,1,4,'20261004',0,1,'商品销售属性组编号',0,NULL,'2026-10-03 14:33:24','system','2026-10-03 14:33:24'),(61,'SALE_PROPERTY_ITEM','销售属性值','SI','',6,1,1,12,'20261004',0,1,'商品销售属性值编号',0,NULL,'2026-10-03 14:33:24','system','2026-10-03 14:33:24'),(62,'STOCK_TAKE_PLAN','库存盘点任务','PD','yyyyMMdd',4,1,1,6,'20261003',1,1,'库存盘点任务编号',0,NULL,'2026-10-03 15:06:18','system','2026-10-03 15:06:18'),(63,'STOCK_TAKE_PRE','预先盘点单','YPD','yyyyMMdd',4,1,1,2,'20261003',1,1,'预先盘点单编号',0,NULL,'2026-10-03 15:06:18','system','2026-10-03 15:06:18'),(64,'STOCK_COST_ADJUST','库存成本调整单','CB','yyyyMMdd',4,1,1,0,'',1,1,'库存成本调整单编号',0,NULL,'2026-10-03 15:06:18','system','2026-10-03 15:06:18');
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

-- Dump completed on 2026-10-06 14:48:32
