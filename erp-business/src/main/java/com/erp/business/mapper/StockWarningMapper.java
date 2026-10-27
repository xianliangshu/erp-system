package com.erp.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.business.entity.StockWarning;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库存预警 Mapper
 *
 * @author ERP System
 */
@Mapper
public interface StockWarningMapper extends BaseMapper<StockWarning> {

    /**
     * 实时命中查询：把预警配置与当前库存比对，只返回已超限的记录。
     *
     * <p>刻意采用「查询时实时算」而非「出入库时挂勾子落记录」——
     * 后者要改动 12 个库存调用点，前者对老链路完全零侵入。</p>
     *
     * <p>注意：商品没有库存记录时按 0 处理，因此「下限 &gt; 0 且无库存」会命中缺货预警，这是预期行为。</p>
     */
    @Select("<script>" +
            "SELECT w.id, w.sc_id, w.product_id, w.max_limit, w.min_limit, w.status, w.remark, " +
            "       w.create_time, w.update_time, " +
            "       wh.name AS sc_name, " +
            "       m.code AS product_code, m.name AS product_name, " +
            "       m.specification AS specification, m.sale_property_text AS sale_property_text, " +
            "       COALESCE(s.stock_num, 0) AS stock_num, " +
            "       CASE WHEN w.min_limit IS NOT NULL AND COALESCE(s.stock_num, 0) &lt; w.min_limit THEN 1 " +
            "            WHEN w.max_limit IS NOT NULL AND COALESCE(s.stock_num, 0) &gt; w.max_limit THEN 2 " +
            "            ELSE 0 END AS hit_type " +
            "FROM stock_warning w " +
            "LEFT JOIN product_stock s ON s.sc_id = w.sc_id AND s.product_id = w.product_id AND s.deleted = 0 " +
            "LEFT JOIN base_material m ON m.id = w.product_id AND m.deleted = 0 " +
            "LEFT JOIN base_warehouse wh ON wh.id = w.sc_id " +
            "WHERE w.deleted = 0 AND w.status = 1 " +
            "  AND ((w.min_limit IS NOT NULL AND COALESCE(s.stock_num, 0) &lt; w.min_limit) " +
            "    OR (w.max_limit IS NOT NULL AND COALESCE(s.stock_num, 0) &gt; w.max_limit)) " +
            "<if test='scId != null'> AND w.sc_id = #{scId} </if>" +
            "<if test='productName != null and productName.length() > 0'> " +
            "  AND m.name LIKE CONCAT('%', #{productName}, '%') </if>" +
            "ORDER BY hit_type, m.code" +
            "</script>")
    List<StockWarning> selectHitList(@Param("scId") Long scId, @Param("productName") String productName);
}
