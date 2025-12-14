-- 物料信息表
CREATE TABLE IF NOT EXISTS `base_material` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物料ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `code` VARCHAR(50) NOT NULL COMMENT '物料编号',
    `name` VARCHAR(200) NOT NULL COMMENT '物料名称',
    `short_name` VARCHAR(100) DEFAULT NULL COMMENT '物料简称',
    `specification` VARCHAR(200) DEFAULT NULL COMMENT '规格型号',
    `unit_id` BIGINT NOT NULL COMMENT '计量单位ID',
    `purchase_price` DECIMAL(18,2) DEFAULT 0.00 COMMENT '采购价格',
    `sale_price` DECIMAL(18,2) DEFAULT 0.00 COMMENT '销售价格',
    `retail_price` DECIMAL(18,2) DEFAULT 0.00 COMMENT '零售价格',
    `min_stock` DECIMAL(18,2) DEFAULT 0.00 COMMENT '最低库存',
    `max_stock` DECIMAL(18,2) DEFAULT 0.00 COMMENT '最高库存',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记(0=未删除,1=已删除)',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_unit_id` (`unit_id`),
    KEY `idx_status` (`status`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料信息表';

-- 插入测试数据
INSERT INTO `base_material` (`category_id`, `code`, `name`, `short_name`, `specification`, `unit_id`, `purchase_price`, `sale_price`, `retail_price`, `min_stock`, `max_stock`, `status`, `remark`) VALUES
(1, 'MAT000001', '钢板Q235', '钢板', '1200*2400*3mm', 5, 4500.00, 5000.00, 5500.00, 100.00, 1000.00, 1, '普通碳素结构钢板'),
(1, 'MAT000002', '铝合金6061', '铝合金', '1000*2000*2mm', 5, 28000.00, 32000.00, 35000.00, 50.00, 500.00, 1, '铝镁硅合金'),
(4, 'MAT000003', '螺栓M8*20', '螺栓', 'M8*20 304不锈钢', 1, 0.50, 0.80, 1.00, 1000.00, 10000.00, 1, '不锈钢螺栓'),
(5, 'MAT000004', '纸箱', '纸箱', '500*400*300mm', 1, 5.00, 8.00, 10.00, 100.00, 1000.00, 1, '五层瓦楞纸箱');
