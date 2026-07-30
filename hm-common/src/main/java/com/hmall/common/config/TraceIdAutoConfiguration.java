package com.hmall.common.config;

import com.hmall.common.filter.TraceIdFilter;
import com.hmall.common.utils.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            // Feign 调用时透传当前用户id
            Long userId = UserContext.getUser();
            if (userId != null) {
                requestTemplate.header("user-info", userId.toString());
            }
        };
    }
}
