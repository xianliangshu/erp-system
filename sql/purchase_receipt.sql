-- 采购收货表 (主表)
CREATE TABLE `purchase_receipt` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `code` varchar(32) NOT NULL COMMENT '收货单编号',
  `order_id` bigint NOT NULL COMMENT '采购订单ID',
  `order_code` varchar(32) DEFAULT NULL COMMENT '采购订单编号',
  `sc_id` bigint NOT NULL COMMENT '仓库ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `total_num` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT '收货数量',
  `total_amount` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT '收货金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待确认 1-已确认',
  `description` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_order` (`order_id`),
  KEY `idx_supplier` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购收货表';

-- 采购收货明细表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购收货明细表';
