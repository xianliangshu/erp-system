-- 商品库存表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品库存表';
