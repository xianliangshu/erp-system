package com.erp.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.business.entity.ProductStock;
import com.erp.business.enums.ProductStockBizType;
import java.math.BigDecimal;

/**
 * 商品库存服务
 */
public interface IProductStockService extends IService<ProductStock> {

        /**
         * 增加库存
         * 
         * @param scId      仓库ID
         * @param productId 商品ID
         * @param stockNum  增加数量
         * @param taxPrice  含税价格
         * @param bizId     业务单据ID
         * @param bizCode   业务单据号
         * @param bizType   业务类型
         */
        void addStock(Long scId, Long productId, BigDecimal stockNum, BigDecimal taxPrice, Long bizId, String bizCode,
                        ProductStockBizType bizType);

        /**
         * 减少库存
         * 
         * @param scId      仓库ID
         * @param productId 商品ID
         * @param stockNum  减少数量
         * @param taxPrice  含税价格
         * @param bizId     业务单据ID
         * @param bizCode   业务单据号
         * @param bizType   业务类型
         */
        void subStock(Long scId, Long productId, BigDecimal stockNum, BigDecimal taxPrice, Long bizId, String bizCode,
                        ProductStockBizType bizType);

        /**
         * 分页查询库存（包含关联信息）
         */
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.erp.business.vo.ProductStockVO> getStockPage(
                        Long current, Long size, Long scId, String productName);
}
