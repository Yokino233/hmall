package com.hmall.common.config;

import com.hmall.common.filter.TraceIdFilter;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * traceId 链路追踪自动配置。
 * Feign 请求拦截器：透传 traceId 到下游服务。
 * 仅在 Feign 依赖存在时生效（hm-api 或使用 OpenFeign 的服务）。
 */
@Configuration
@ConditionalOnClass(name = "feign.RequestInterceptor")
public class TraceIdAutoConfiguration {

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
