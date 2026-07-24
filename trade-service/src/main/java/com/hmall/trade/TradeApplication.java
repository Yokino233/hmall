package com.hmall.trade;
import com.hmall.common.advice.CommonExceptionAdvice;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
@Import(CommonExceptionAdvice.class)
@MapperScan("com.hmall.trade.mapper")
@EnableFeignClients(basePackages = "com.hmall.api.client", defaultConfiguration = com.hmall.api.config.DefaultFeignConfig.class)
@SpringBootApplication
public class TradeApplication {
    public static void main(String[] args) { SpringApplication.run(TradeApplication.class, args); }
}
