package com.hmall.gateaway.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.listener.Listener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class NacosDynamicRouteRepository implements RouteDefinitionRepository {

    private final NacosConfigProperties nacosConfig;
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher publisher;
    private static final String DATA_ID = "gateway-routes.json";
    private static final String GROUP = "DEFAULT_GROUP";

    public NacosDynamicRouteRepository(NacosConfigProperties nacosConfig,
                                       ObjectMapper mapper,
                                       ApplicationEventPublisher publisher) {
        this.nacosConfig = nacosConfig;
        this.mapper = mapper;
        this.publisher = publisher;
        listenToNacos();
    }

    private void listenToNacos() {
        try {
            nacosConfig.configServiceInstance().addListener(DATA_ID, GROUP, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("路由配置发生变化，刷新路由...");
                    publisher.publishEvent(new RefreshRoutesEvent(this));
                }
            });
            log.info("已监听 Nacos 路由配置: dataId={}, group={}", DATA_ID, GROUP);
        } catch (Exception e) {
            log.error("监听 Nacos 路由配置失败", e);
        }
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        try {
            String config = nacosConfig.configServiceInstance().getConfig(DATA_ID, GROUP, 5000);
            if (config != null && !config.isEmpty()) {
                List<RouteDefinition> routes = mapper.readValue(config,
                        new TypeReference<List<RouteDefinition>>() {});
                log.info("从 Nacos 加载了 {} 条路由", routes.size());
                return Flux.fromIterable(routes);
            }
        } catch (Exception e) {
            log.error("从 Nacos 加载路由失败", e);
        }
        return Flux.empty();
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) { return Mono.empty(); }
    @Override
    public Mono<Void> delete(Mono<String> routeId) { return Mono.empty(); }
}