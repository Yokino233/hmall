package com.hmall.pay;
import com.hmall.common.advice.CommonExceptionAdvice;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
@Import(CommonExceptionAdvice.class)
@MapperScan("com.hmall.pay.mapper")
@EnableFeignClients(basePackages = "com.hmall.api.client", defaultConfiguration = com.hmall.api.config.DefaultFeignConfig.class)
@SpringBootApplication
public class PayApplication {
    public static void main(String[] args) { SpringApplication.run(PayApplication.class, args); }
}
