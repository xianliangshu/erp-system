-- 采购退货表
CREATE TABLE IF NOT EXISTS `purchase_return` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code` varchar(32) NOT NULL COMMENT '退货单编号',
    `receipt_id` bigint DEFAULT NULL COMMENT '收货单ID',
    `receipt_code` varchar(32) DEFAULT NULL COMMENT '收货单编号',
    `sc_id` bigint DEFAULT NULL COMMENT '仓库ID',
    `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
    `total_num` decimal(16,2) DEFAULT '0.00' COMMENT '退货数量',
    `total_amount` decimal(16,2) DEFAULT '0.00' COMMENT '退货金额',
    `status` tinyint DEFAULT '0' COMMENT '状态: 0-待确认 1-已确认',
    `description` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT '0' COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_receipt_id` (`receipt_id`),
    KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货表';

-- 采购退货明细表
CREATE TABLE IF NOT EXISTS `purchase_return_detail` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货明细表';
