package com.hmall.common.filter;

import cn.hutool.core.util.IdUtil;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Servlet 过滤器：从请求头提取 traceId，放入 MDC 供日志输出。
 * 如果请求头中没有 traceId，说明请求来自外部（如网关），则自动生成。
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@WebFilter("/*")
public class TraceIdFilter implements Filter {

    public static final String TRACE_ID_HEADER = "x-trace-id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);

        // 没有 traceId 则生成（外部请求，如直连或网关过来的首次请求）
        if (traceId == null || traceId.isEmpty()) {
            traceId = IdUtil.fastSimpleUUID();
        }

        MDC.put(TRACE_ID_MDC_KEY, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }
}
