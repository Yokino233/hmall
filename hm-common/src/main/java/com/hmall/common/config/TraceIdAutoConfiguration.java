package com.hmall.common.config;

import com.hmall.common.filter.TraceIdFilter;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * traceId 链路追踪自动配置。
 * 1. 注册 TraceIdFilter（Servlet 过滤器）
 * 2. 注册 Feign 请求拦截器（透传 traceId 到下游服务）
 */
@Configuration
public class TraceIdAutoConfiguration {

    /**
     * 注册 Servlet Filter：从请求头提取 traceId 并放入 MDC
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.setName("traceIdFilter");
        return registration;
    }

    /**
     * Feign 请求拦截器：将当前 MDC 中的 traceId 写入 Feign 请求头，
     * 使得下游服务能通过 TraceIdFilter 获取到同一个 traceId。
     */
    @Bean
    public RequestInterceptor traceIdRequestInterceptor() {
        return requestTemplate -> {
            String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
            if (traceId != null) {
                requestTemplate.header(TraceIdFilter.TRACE_ID_HEADER, traceId);
            }
        };
    }
}
