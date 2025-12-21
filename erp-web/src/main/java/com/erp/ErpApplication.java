package com.erp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ERP系统启动类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@SpringBootApplication
@MapperScan({ "com.erp.*.mapper" })
public class ErpApplication {
    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
    }
}