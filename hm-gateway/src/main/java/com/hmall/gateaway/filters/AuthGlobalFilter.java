package com.hmall.gateaway.filters;

import com.hmall.gateaway.config.AuthProperties;
import com.hmall.common.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtTool jwtTool;
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取request
        ServerHttpRequest request = exchange.getRequest();
        // 判断是否需要做登录拦截
        String path = request.getPath().value();
        if (isExclude(path)) {
            // 白名单路径，直接放行
            return chain.filter(exchange);
        }

        // 获取token
        HttpHeaders headers = request.getHeaders();
        List<String> tokenList = headers.get("authorization");
        if (tokenList == null || tokenList.isEmpty()) {
            // 没有token，未登录
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = tokenList.get(0);
        // 去掉 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 校验并解析token
        Long userId;
        try {
            userId = jwtTool.parseToken(token);
        } catch (Exception e) {
            // token解析失败,设置状态码为401
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 传递用户信息 - 将用户信息写入请求头
        String userInfo = userId.toString();
        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();


        // 放行
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        // 白名单路径列表
        for(String pathPattern : authProperties.getExcludePaths()){
            if(antPathMatcher.match(pathPattern, path)) {
                return true;
            }
        }
        return false;
    }
}
