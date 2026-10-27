
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

LOCK TABLES `base_material` WRITE;
/*!40000 ALTER TABLE `base_material` DISABLE KEYS */;
INSERT INTO `base_material` VALUES (1,1,'MAT000001','钢板Q235','钢板','1200*2400*3mm',5,4500.00,5000.00,5500.00,100.00,1000.00,1,'普通碳素结构钢板',0,NULL,'2026-08-01 19:23:55',NULL,'2026-08-01 19:23:55'),(2,1,'MAT000002','铝合金6061','铝合金','1000*2000*2mm',5,28000.00,32000.00,35000.00,50.00,500.00,1,'铝镁硅合金',0,NULL,'2026-08-01 19:23:55',NULL,'2026-08-01 19:23:55'),(3,4,'MAT000003','螺栓M8*20','螺栓','M8*20 304不锈钢',1,0.50,0.80,1.00,1000.00,10000.00,1,'不锈钢螺栓',0,NULL,'2026-08-01 19:23:55',NULL,'2026-08-01 19:23:55'),(4,5,'MAT000004','纸箱','纸箱','500*400*300mm',1,5.00,8.00,10.00,100.00,1000.00,1,'五层瓦楞纸箱',0,NULL,'2026-08-01 19:23:55',NULL,'2026-08-01 19:23:55'),(25,1,'MAT000005','测试物料甲','甲','100*100',5,10.50,15.00,18.00,1.00,100.00,1,'导入测试',0,'system','2026-10-03 12:47:04','system','2026-10-03 12:47:04'),(26,3,'MAT000006','测试物料乙',NULL,'200*200',1,20.00,30.00,35.00,2.00,200.00,0,'导入测试',0,'system','2026-10-03 12:47:04','system','2026-10-03 12:47:04'),(27,4,'MAT000007','测试物料丙','丙',NULL,4,5.00,8.00,9.00,0.00,50.00,1,'状态留空走默认启用',0,'system','2026-10-03 12:47:05','system','2026-10-03 12:47:05');
/*!40000 ALTER TABLE `base_material` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `base_customer` WRITE;
/*!40000 ALTER TABLE `base_customer` DISABLE KEYS */;
INSERT INTO `base_customer` VALUES (1,'CUS000001','北京京东世纪贸易有限公司','京东',3,1,'刘经理','010-12345678',NULL,'北京市大兴区亦庄经济技术开发区',1,0,1,1,'核心分销商','2026-08-01 19:23:45','2026-08-01 19:23:45',NULL,NULL,0),(2,'CUS000002','阿里巴巴（中国）网络技术有限公司','阿里',3,1,'马经理','0571-87654321',NULL,'杭州市余杭区文一西路969号',1,0,1,2,'核心分销商','2026-08-01 19:23:45','2026-08-01 19:23:45',NULL,NULL,0),(3,'CUS000003','普通零售客户A','客户A',1,3,'张三','13800138000',NULL,'上海市浦东新区',1,0,1,3,'普通客户','2026-08-01 19:23:45','2026-08-01 19:23:45',NULL,NULL,0);
/*!40000 ALTER TABLE `base_customer` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `base_supplier` WRITE;
/*!40000 ALTER TABLE `base_supplier` DISABLE KEYS */;
INSERT INTO `base_supplier` VALUES (1,'SUP000001','深圳市华强电子有限公司','华强电子',1,1,'张经理','0755-12345678','zhang@huaqiang.com','深圳市福田区华强北路1号',NULL,2,30,NULL,NULL,NULL,1,1,'主要供应商','2026-08-01 19:24:14','2026-08-01 19:24:14',NULL,NULL,0),(2,'SUP000002','广州市天河机械设备公司','天河机械',2,2,'李工','020-87654321','li@tianhe.com','广州市天河区天河路100号',NULL,3,90,NULL,NULL,NULL,1,2,'设备供应商','2026-08-01 19:24:14','2026-08-01 19:24:14',NULL,NULL,0),(3,'SUP000003','东莞市长安物流服务公司','长安物流',3,3,'王主管','0769-98765432','wang@changan.com','东莞市长安镇长安大道200号',NULL,1,0,NULL,NULL,NULL,1,3,'物流服务商','2026-08-01 19:24:14','2026-08-01 19:24:14',NULL,NULL,0);
/*!40000 ALTER TABLE `base_supplier` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_code_rule` WRITE;
/*!40000 ALTER TABLE `sys_code_rule` DISABLE KEYS */;
INSERT INTO `sys_code_rule` VALUES (1,'PURCHASE_ORDER','采购订单','PO','yyyyMMdd',4,1,1,1,'20261001',1,1,'原 PurchaseOrderServiceImpl',0,NULL,NULL,'system','2026-10-03 12:30:39'),(2,'PURCHASE_RECEIPT','采购收货单','PR','yyyyMMdd',4,1,1,1,'20261001',1,1,'原 PurchaseReceiptServiceImpl',0,NULL,NULL,NULL,NULL),(3,'PURCHASE_RETURN','采购退货单','PTH','yyyyMMdd',4,1,1,0,'',1,1,'原 PurchaseReturnServiceImpl',0,NULL,NULL,NULL,NULL),(4,'SALE_ORDER','销售订单','SO','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleOrderServiceImpl',0,NULL,NULL,NULL,NULL),(5,'SALE_DELIVERY','销售出库单','SD','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleDeliveryServiceImpl',0,NULL,NULL,NULL,NULL),(6,'SALE_RETURN','销售退货单','SR','yyyyMMdd',4,1,1,0,'',1,1,'原 SaleReturnServiceImpl',0,NULL,NULL,NULL,NULL),(7,'RETAIL_OUT','零售出库单','LS','yyyyMMdd',4,1,1,0,'',1,1,'原 RetailOutSheetServiceImpl',0,NULL,NULL,NULL,NULL),(8,'RETAIL_RETURN','零售退货单','LT','yyyyMMdd',4,1,1,0,'',1,1,'原 RetailReturnServiceImpl',0,NULL,NULL,NULL,NULL),(9,'STOCK_CHECK','库存盘点单','SC','yyyyMMdd',4,1,1,0,'',1,1,'原 StockCheckServiceImpl',0,NULL,NULL,NULL,NULL),(10,'STOCK_TRANSFER','库存调拨单','ST','yyyyMMdd',4,1,1,0,'',1,1,'原 StockTransferServiceImpl',0,NULL,NULL,NULL,NULL),(11,'STOCK_ADJUST','库存调整单','SA','yyyyMMdd',4,1,1,0,'',1,1,'原 StockAdjustSheetServiceImpl',0,NULL,NULL,NULL,NULL),(12,'SETTLE_SHEET','供应商结算单','JS','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleSheetServiceImpl',0,NULL,NULL,NULL,NULL),(13,'SETTLE_CHECK','供应商对账单','DZ','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleCheckSheetServiceImpl',0,NULL,NULL,NULL,NULL),(14,'SETTLE_FEE','供应商费用单','FY','yyyyMMdd',4,1,1,0,'',1,1,'原 SettleFeeSheetServiceImpl',0,NULL,NULL,NULL,NULL),(15,'SETTLE_PRE','供应商预收预付单','YF','yyyyMMdd',4,1,1,0,'',1,1,'原 SettlePreSheetServiceImpl',0,NULL,NULL,NULL,NULL),(16,'CUSTOMER_SETTLE','客户结算单','KJS','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerSettleSheetServiceImpl',0,NULL,NULL,NULL,NULL),(17,'CUSTOMER_CHECK','客户对账单','KDZ','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerCheckSheetServiceImpl',0,NULL,NULL,NULL,NULL),(18,'CUSTOMER_FEE_IN','客户费用单(收入)','KSK','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerFeeSheetServiceImpl sheetType=1',0,NULL,NULL,NULL,NULL),(19,'CUSTOMER_FEE_OUT','客户费用单(支出)','KKK','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerFeeSheetServiceImpl sheetType!=1',0,NULL,NULL,NULL,NULL),(20,'CUSTOMER_PRE','客户预收预付单','KYS','yyyyMMdd',4,1,1,0,'',1,1,'原 CustomerPreSheetServiceImpl',0,NULL,NULL,NULL,NULL),(21,'SETTLE_IN_OUT_ITEM','收支项目','SZ','',4,1,1,0,'',0,1,'原 SettleInOutItemServiceImpl 无日期段',0,NULL,NULL,NULL,NULL),(22,'STOCK_ADJUST_REASON','库存调整原因','','',3,1,1,0,'',0,1,'原 StockAdjustReasonServiceImpl 3位纯流水',0,NULL,NULL,NULL,NULL),(23,'BASE_MATERIAL','物料','MAT','',6,1,1,7,'20261003',0,1,'原 BaseMaterialServiceImpl',0,NULL,NULL,'system','2026-10-03 12:30:06'),(24,'BASE_CUSTOMER','客户','CUS','',6,1,1,3,'',0,1,'原 BaseCustomerServiceImpl',0,NULL,NULL,NULL,NULL),(25,'BASE_SUPPLIER','供应商','SUP','',6,1,1,3,'',0,1,'原 BaseSupplierServiceImpl',0,NULL,NULL,NULL,NULL),(26,'BASE_WAREHOUSE','仓库','WH','',6,1,1,4,'',0,1,'原 BaseWarehouseServiceImpl',0,NULL,NULL,NULL,NULL),(27,'BASE_BRAND','品牌','BRD','',6,1,1,3,'',0,1,'原 BaseBrandServiceImpl',0,NULL,NULL,NULL,NULL),(28,'BASE_UNIT','计量单位','UNIT','',6,1,1,15,'',0,1,'原 BaseUnitServiceImpl',0,NULL,NULL,NULL,NULL),(29,'BASE_MATERIAL_CATEGORY','物料分类','MC','',6,1,1,5,'',0,1,'原 BaseMaterialCategoryServiceImpl',0,NULL,NULL,NULL,NULL);
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

