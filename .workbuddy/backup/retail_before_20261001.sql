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
-- Table structure for table `retail_config`
--

DROP TABLE IF EXISTS `retail_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `retail_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `out_stock_un_approve` tinyint(1) DEFAULT '0' COMMENT '零售出库单是否自动审核',
  `return_stock_un_approve` tinyint(1) DEFAULT '0' COMMENT '零售退货单是否自动审核',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='零售配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `retail_config`
--

LOCK TABLES `retail_config` WRITE;
/*!40000 ALTER TABLE `retail_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `retail_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `retail_out_sheet`
--

DROP TABLE IF EXISTS `retail_out_sheet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `retail_out_sheet` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '单据编号',
  `sc_id` bigint NOT NULL COMMENT '仓库ID',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `total_num` decimal(18,4) DEFAULT '0.0000' COMMENT '总数量',
  `total_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '总金额',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-待审核 1-已审核 2-已拒绝',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='零售出库单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `retail_out_sheet`
--

LOCK TABLES `retail_out_sheet` WRITE;
/*!40000 ALTER TABLE `retail_out_sheet` DISABLE KEYS */;
/*!40000 ALTER TABLE `retail_out_sheet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `retail_return`
--

DROP TABLE IF EXISTS `retail_return`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `retail_return` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '单据编号',
  `out_sheet_id` bigint DEFAULT NULL COMMENT '关联出库单ID',
  `sc_id` bigint NOT NULL COMMENT '仓库ID',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `total_num` decimal(18,4) DEFAULT '0.0000' COMMENT '总数量',
  `total_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '总金额',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-待审核 1-已审核 2-已拒绝',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='零售退货单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `retail_return`
--

LOCK TABLES `retail_return` WRITE;
/*!40000 ALTER TABLE `retail_return` DISABLE KEYS */;
/*!40000 ALTER TABLE `retail_return` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `retail_out_sheet_detail`
--

DROP TABLE IF EXISTS `retail_out_sheet_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `retail_out_sheet_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sheet_id` bigint NOT NULL COMMENT '主表ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `out_num` decimal(18,4) NOT NULL COMMENT '出库数量',
  `tax_price` decimal(18,4) DEFAULT '0.0000' COMMENT '含税单价',
  `tax_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '含税金额',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='零售出库单明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `retail_out_sheet_detail`
--

LOCK TABLES `retail_out_sheet_detail` WRITE;
/*!40000 ALTER TABLE `retail_out_sheet_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `retail_out_sheet_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `retail_return_detail`
--

DROP TABLE IF EXISTS `retail_return_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `retail_return_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `return_id` bigint NOT NULL COMMENT '主表ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `return_num` decimal(18,4) NOT NULL COMMENT '退货数量',
  `tax_price` decimal(18,4) DEFAULT '0.0000' COMMENT '含税单价',
  `tax_amount` decimal(18,4) DEFAULT '0.0000' COMMENT '含税金额',
  `description` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='零售退货单明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `retail_return_detail`
--

LOCK TABLES `retail_return_detail` WRITE;
/*!40000 ALTER TABLE `retail_return_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `retail_return_detail` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-01 19:36:09
