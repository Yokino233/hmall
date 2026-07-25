package com.hmall.cart;

import com.hmall.common.advice.CommonExceptionAdvice;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@EnableRabbit
@EnableFeignClients(basePackages = "com.hmall.api.client", defaultConfiguration = com.hmall.api.config.DefaultFeignConfig.class)
@Import(CommonExceptionAdvice.class)
@MapperScan("com.hmall.cart.mapper")
@SpringBootApplication
public class CartApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }
}
