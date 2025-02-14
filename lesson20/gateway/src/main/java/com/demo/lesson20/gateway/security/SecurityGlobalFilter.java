package com.demo.lesson20.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 其它处理逻辑，可作为鉴权后的后续处理
 */
@Component
@Slf4j
public class SecurityGlobalFilter implements GlobalFilter, Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("鉴权后处理");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
