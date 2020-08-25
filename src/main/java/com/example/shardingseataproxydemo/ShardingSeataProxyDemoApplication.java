package com.example.shardingseataproxydemo;

import org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@MapperScan("com.example.shardingseataproxydemo.repository.mapper")
@EnableTransactionManagement
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, SpringBootConfiguration.class, JtaAutoConfiguration.class})
public class ShardingSeataProxyDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShardingSeataProxyDemoApplication.class, args);
        System.out.println("服务启动..~~");
    }

}
