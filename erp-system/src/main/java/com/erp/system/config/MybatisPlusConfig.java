package com.erp.system.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置MyBatis-Plus拦截器
     * 包含分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置请求的页面大于最大页后操作,true调回到首页,false继续请求,默认false
        paginationInterceptor.setOverflow(false);
        // 设置最大单页限制数量,默认500条,-1不受限制
        paginationInterceptor.setMaxLimit(1000L);

        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}
