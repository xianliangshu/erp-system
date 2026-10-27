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
         * 增加库存，但 <b>不更新成本价</b>。
         * <p>
         * 适用于销售退货 / 零售退货这类"按售价回冲"的入库场景：数量要加回去，
         * 但价格是售价、不是成本，不能用它覆盖库存成本价。
         *
         * @param scId      仓库ID
         * @param productId 商品ID
         * @param stockNum  增加数量
         * @param taxPrice  含税价格（仅在该仓库+商品尚无库存记录时作为初始成本价）
         * @param bizId     业务单据ID
         * @param bizCode   业务单据号
         * @param bizType   业务类型
         */
        void addStockKeepCost(Long scId, Long productId, BigDecimal stockNum, BigDecimal taxPrice, Long bizId,
                        String bizCode, ProductStockBizType bizType);

        /**
         * 校验库存是否充足，不足时抛出 BusinessException。
         * <p>
         * 出库类业务（销售出库、零售出库、采购退货…）在扣减库存前应先调用，
         * 避免把库存扣成负数。
         *
         * @param scId       仓库ID
         * @param productId  商品ID
         * @param requireNum 本次需要的数量（正数）
         */
        void checkStockEnough(Long scId, Long productId, BigDecimal requireNum);

        /**
         * 分页查询库存（包含关联信息）
         */
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.erp.business.vo.ProductStockVO> getStockPage(
                        Long current, Long size, Long scId, String productName);
}
