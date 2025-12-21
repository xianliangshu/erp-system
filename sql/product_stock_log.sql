-- 商品库存日志表
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
  KEY `idx_biz` (`biz_id`, `biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品库存日志表';
