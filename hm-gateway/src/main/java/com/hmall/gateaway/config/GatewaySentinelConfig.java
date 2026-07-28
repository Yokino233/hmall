package com.hmall.gateaway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
public class GatewaySentinelConfig {

    @PostConstruct
    public void init() {
        // 1. 定义 API 分组
        Set<ApiDefinition> apis = new HashSet<>();

        Set<ApiPredicateItem> searchPredicates = new HashSet<>();
        searchPredicates.add(new ApiPathPredicateItem()
                .setPattern("/items/search/**")
                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
        apis.add(new ApiDefinition("search_api").setPredicateItems(searchPredicates));

        Set<ApiPredicateItem> orderPredicates = new HashSet<>();
        orderPredicates.add(new ApiPathPredicateItem()
                .setPattern("/orders/**")
                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
        apis.add(new ApiDefinition("order_api").setPredicateItems(orderPredicates));

        Set<ApiPredicateItem> loginPredicates = new HashSet<>();
        loginPredicates.add(new ApiPathPredicateItem()
                .setPattern("/users/login")
                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT));
        apis.add(new ApiDefinition("login_api").setPredicateItems(loginPredicates));

        GatewayApiDefinitionManager.loadApiDefinitions(apis);

        // 2. 限流规则
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("search_api").setCount(100).setIntervalSec(1));
        rules.add(new GatewayFlowRule("order_api").setCount(50).setIntervalSec(1));
        rules.add(new GatewayFlowRule("login_api").setCount(20).setIntervalSec(1));

        GatewayRuleManager.loadRules(rules);
        log.info("Sentinel Gateway 限流规则已加载: search=100/s, order=50/s, login=20/s");
    }
}
