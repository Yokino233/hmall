package com.hmall.common.config;

import com.hmall.common.filter.TraceIdFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Servlet 环境专用配置：注册 TraceIdFilter。
 * 仅在 javax.servlet.Filter 可用时加载（即 Spring MVC 服务）。
 * Gateway（WebFlux）会自动跳过。
 */
@Configuration
@ConditionalOnClass(name = "javax.servlet.Filter")
public class TraceIdServletConfiguration {

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.setName("traceIdFilter");
        return registration;
    }
}
