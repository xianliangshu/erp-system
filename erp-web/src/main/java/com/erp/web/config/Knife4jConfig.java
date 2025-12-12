package com.erp.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置API文档基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("跨境电商ERP系统 API文档")
                        .description("基于Spring Boot 3.2.0 + MyBatis-Plus的ERP系统接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("ERP开发团队")
                                .email("dev@erp.com")
                                .url("https://www.erp.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    /**
     * 系统管理模块API分组
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("1-系统管理")
                .pathsToMatch("/system/**")
                .build();
    }

    /**
     * 业务管理模块API分组
     */
    @Bean
    public GroupedOpenApi businessApi() {
        return GroupedOpenApi.builder()
                .group("2-业务管理")
                .pathsToMatch("/business/**")
                .build();
    }

    /**
     * 全部API分组
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("0-全部接口")
                .pathsToMatch("/**")
                .build();
    }
}
