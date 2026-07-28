package com.hmall.gateaway.filters;

import cn.hutool.core.util.IdUtil;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway 全局过滤器：为每个请求生成 traceId，放入请求头传给下游。
 * 优先级最高（Ordered.HIGHEST_PRECEDENCE），在 AuthGlobalFilter 之前执行。
 */
@Component
public class TraceIdGatewayFilter implements GlobalFilter, Ordered {

    public static final String TRACE_ID_HEADER = "x-trace-id";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 生成 traceId
        String traceId = IdUtil.fastSimpleUUID();

        // 放入 MDC（gateway 自身日志也能输出 traceId）
        MDC.put("traceId", traceId);

        // 写入请求头，透传给下游服务
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(TRACE_ID_HEADER, traceId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request)
                .build();

        return chain.filter(mutatedExchange)
                .doFinally(signalType -> MDC.remove("traceId"));
    }
}
