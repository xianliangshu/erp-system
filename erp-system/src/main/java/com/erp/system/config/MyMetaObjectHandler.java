package com.erp.system.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充配置
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 自动填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // TODO: 从当前登录用户获取用户名
        // 自动填充创建人
        this.strictInsertFill(metaObject, "createBy", String.class, "system");
        // 自动填充更新人
        this.strictInsertFill(metaObject, "updateBy", String.class, "system");
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // TODO: 从当前登录用户获取用户名
        // 自动填充更新人
        this.strictUpdateFill(metaObject, "updateBy", String.class, "system");
    }
}
