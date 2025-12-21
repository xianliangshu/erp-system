-- 采购订单表 (主表)
CREATE TABLE `purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `code` varchar(32) NOT NULL COMMENT '订单编号',
  `sc_id` bigint NOT NULL COMMENT '仓库ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `purchaser_id` bigint DEFAULT NULL COMMENT '采购员ID',
  `expect_arrive_date` date DEFAULT NULL COMMENT '预计到货日期',
  `total_num` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT '采购数量',
  `total_amount` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT '采购金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待审核 1-已审核 2-已拒绝 3-已完成 4-已取消',
  `description` varchar(500) DEFAULT NULL COMMENT '备注',
  `approve_by` varchar(64) DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `refuse_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_supplier` (`supplier_id`),
  KEY `idx_sc` (`sc_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

-- 采购订单明细表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';
