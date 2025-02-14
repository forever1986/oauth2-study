package com.demo.lesson20.gateway.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.shaded.com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * nacos路由数据源
 */
@Component
public class NacosRouteDefinitionRepository implements RouteDefinitionRepository {

    private static final Logger logger = LoggerFactory.getLogger(NacosRouteDefinitionRepository.class);

    private static final String SCG_DATA_ID = "gateway-lesson20";
    private static final String SCG_GROUP_ID = "DEFAULT_GROUP";

    private final ApplicationEventPublisher publisher;

    private final NacosConfigManager nacosConfigManager;

    public NacosRouteDefinitionRepository(ApplicationEventPublisher publisher, NacosConfigManager nacosConfigManager) {
        this.publisher = publisher;
        this.nacosConfigManager = nacosConfigManager;
        addListener();
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> routeDefinitionList = new ArrayList<>(0);
        try {
            String configContent = nacosConfigManager.getConfigService().getConfig(SCG_DATA_ID, SCG_GROUP_ID, 5000);

            if (!Strings.isNullOrEmpty(configContent)) {
                routeDefinitionList = JSON.parseArray(configContent, RouteDefinition.class);
            }
        } catch (NacosException e) {
            logger.error("从Nacos加载配置的动态路由信息异常", e);
        }
        return Flux.fromIterable(routeDefinitionList);
    }

    /**
     * 添加Nacos监听
     */
    private void addListener() {
        try {
            nacosConfigManager.getConfigService()
                    .addListener(SCG_DATA_ID, SCG_GROUP_ID, new Listener() {

                        @Override
                        public Executor getExecutor() {
                            return null;
                        }

                        @Override
                        public void receiveConfigInfo(String configInfo) {
                            publisher.publishEvent(new RefreshRoutesEvent(this));
                        }
                    });
        } catch (NacosException e) {
            logger.error("添加Nacos监听异常", e);
        }
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return null;
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return null;
    }
}
