
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

LOCK TABLES `product_stock` WRITE;
/*!40000 ALTER TABLE `product_stock` DISABLE KEYS */;
INSERT INTO `product_stock` VALUES (1,2,1,90.0000,110.0000,9900.0000,'system','2026-10-01 22:44:47','system','2026-10-01 22:44:47',0);
/*!40000 ALTER TABLE `product_stock` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `product_stock_log` WRITE;
/*!40000 ALTER TABLE `product_stock_log` DISABLE KEYS */;
INSERT INTO `product_stock_log` VALUES (1,2,1,0.0000,100.0000,100.0000,1,'PR202610010001',1,'system','2026-10-01 22:44:47'),(2,2,1,100.0000,90.0000,10.0000,1,'PTH202610030001',2,'system','2026-10-03 13:50:12');
/*!40000 ALTER TABLE `product_stock_log` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sale_order` WRITE;
/*!40000 ALTER TABLE `sale_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `sale_order` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sale_order_detail` WRITE;
/*!40000 ALTER TABLE `sale_order_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `sale_order_detail` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sale_delivery` WRITE;
/*!40000 ALTER TABLE `sale_delivery` DISABLE KEYS */;
/*!40000 ALTER TABLE `sale_delivery` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sale_delivery_detail` WRITE;
/*!40000 ALTER TABLE `sale_delivery_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `sale_delivery_detail` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sale_return` WRITE;
/*!40000 ALTER TABLE `sale_return` DISABLE KEYS */;
/*!40000 ALTER TABLE `sale_return` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sale_return_detail` WRITE;
/*!40000 ALTER TABLE `sale_return_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `sale_return_detail` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `stock_adjust_sheet` WRITE;
/*!40000 ALTER TABLE `stock_adjust_sheet` DISABLE KEYS */;
/*!40000 ALTER TABLE `stock_adjust_sheet` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `stock_adjust_sheet_detail` WRITE;
/*!40000 ALTER TABLE `stock_adjust_sheet_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `stock_adjust_sheet_detail` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_code_rule` WRITE;
/*!40000 ALTER TABLE `sys_code_rule` DISABLE KEYS */;
INSERT INTO `sys_code_rule` VALUES (1,'PURCHASE_ORDER','采购订单','PO','yyyyMMdd',4,1,1,1,'20261001',1,1,'原 PurchaseOrderServiceImpl',0,NULL,NULL,'system','2026-10-03 12:30:39'),(2,'PURCHASE_RECEIPT','采购收货单','PR','yyyyMMdd',4,1,1,1,'20261001',1,1,'原 PurchaseReceiptServiceImpl',0,NULL,NULL,NULL,NULL),(3,'PURCHASE_RETURN','采购退货单','PTH','yyyyMMdd',4,1,1,1,'20261003',1,1,'原 PurchaseReturnServiceImpl',0,NULL,NULL,'system','2026-10-03 13:49:45'),(4,'SALE_ORDER','销售订单','SO','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleOrderServiceImpl',0,NULL,NULL,NULL,NULL),(5,'SALE_DELIVERY','销售出库单','SD','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleDeliveryServiceImpl',0,NULL,NULL,NULL,NULL),(6,'SALE_RETURN','销售退货单','SR','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleReturnServiceImpl',0,NULL,NULL,NULL,NULL),(7,'RETAIL_OUT','零售出库单','LS','yyyyMMdd',4,1,1,0,'',1,1,'原 RetailOutSheetServiceImpl',0,NULL,NULL,NULL,NULL),(8,'RETAIL_RETURN','零售退货单','LT','yyyyMMdd',4,1,1,0,'',1,1,'原 RetailReturnServiceImpl',0,NULL,NULL,NULL,NULL),(9,'STOCK_CHECK','库存盘点单','SC','yyyyMMdd',4,1,1,0,'',1,1,'原 StockCheckServiceImpl',0,NULL,NULL,NULL,NULL),(10,'STOCK_TRANSFER','库存调拨单','ST','yyyyMMdd',4,1,1,0,'',1,1,'原 StockTransferServiceImpl',0,NULL,NULL,NULL,NULL),(11,'STOCK_ADJUST','库存调整单','SA','yyyyMMdd',4,1,1,0,'',1,1,'原 StockAdjustSheetServiceImpl',0,NULL,NULL,NULL,NULL),(12,'SETTLE_SHEET','供应商结算单','JS','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleSheetServiceImpl',0,NULL,NULL,NULL,NULL),(13,'SETTLE_CHECK','供应商对账单','DZ','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleCheckSheetServiceImpl',0,NULL,NULL,NULL,NULL),(14,'SETTLE_FEE','供应商费用单','FY','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleFeeSheetServiceImpl',0,NULL,NULL,NULL,NULL),(15,'SETTLE_PRE','供应商预收预付单','YF','yyyyMMdd',4,1,1,0,'',1,1,'原 SettlePreSheetServiceImpl',0,NULL,NULL,NULL,NULL),(16,'CUSTOMER_SETTLE','客户结算单','KJS','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerSettleSheetServiceImpl',0,NULL,NULL,NULL,NULL),(17,'CUSTOMER_CHECK','客户对账单','KDZ','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerCheckSheetServiceImpl',0,NULL,NULL,NULL,NULL),(18,'CUSTOMER_FEE_IN','客户费用单(收入)','KSK','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerFeeSheetServiceImpl sheetType=1',0,NULL,NULL,NULL,NULL),(19,'CUSTOMER_FEE_OUT','客户费用单(支出)','KKK','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerFeeSheetServiceImpl sheetType!=1',0,NULL,NULL,NULL,NULL),(20,'CUSTOMER_PRE','客户预收预付单','KYS','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerPreSheetServiceImpl',0,NULL,NULL,NULL,NULL),(21,'SETTLE_IN_OUT_ITEM','收支项目','SZ','',4,1,1,0,'',0,1,'原 SettleInOutItemServiceImpl 无日期段',0,NULL,NULL,NULL,NULL),(22,'STOCK_ADJUST_REASON','库存调整原因','','',3,1,1,0,'',0,1,'原 StockAdjustReasonServiceImpl 3位纯流水',0,NULL,NULL,NULL,NULL),(23,'BASE_MATERIAL','物料','MAT','',6,1,1,4,'20261003',0,1,'原 BaseMaterialServiceImpl',0,NULL,NULL,'system','2026-10-03 12:30:06'),(24,'BASE_CUSTOMER','客户','CUS','',6,1,1,3,'',0,1,'原 BaseCustomerServiceImpl',0,NULL,NULL,'system','2026-10-03 12:47:42'),(25,'BASE_SUPPLIER','供应商','SUP','',6,1,1,3,'',0,1,'原 BaseSupplierServiceImpl',0,NULL,NULL,'system','2026-10-03 12:47:42'),(26,'BASE_WAREHOUSE','仓库','WH','',6,1,1,4,'',0,1,'原 BaseWarehouseServiceImpl',0,NULL,NULL,NULL,NULL),(27,'BASE_BRAND','品牌','BRD','',6,1,1,3,'',0,1,'原 BaseBrandServiceImpl',0,NULL,NULL,NULL,NULL),(28,'BASE_UNIT','计量单位','UNIT','',6,1,1,15,'',0,1,'原 BaseUnitServiceImpl',0,NULL,NULL,NULL,NULL),(29,'BASE_MATERIAL_CATEGORY','物料分类','MC','',6,1,1,5,'',0,1,'原 BaseMaterialCategoryServiceImpl',0,NULL,NULL,NULL,NULL);
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

