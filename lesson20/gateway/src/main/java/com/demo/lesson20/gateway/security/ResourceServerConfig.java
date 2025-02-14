package com.demo.lesson20.gateway.security;

import com.demo.lesson20.gateway.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity //注意该注解，是使用Flux方式
public class ResourceServerConfig {

    private final ResourceServerManager resourceServerManager;


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        // 本地获取公钥
        http.oauth2ResourceServer(server ->
                server.jwt(jwt->jwt.jwtDecoder(jwtDecoder()))
        );
        // 配置路由拦截
        http.authorizeExchange(exchange->
                // 设置resourceServerManager
                exchange.anyExchange().access(resourceServerManager))
                // 处理未授权
                .exceptionHandling(ex->ex.accessDeniedHandler(accessDeniedHandler()))
                // 关闭csrf
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                ;

        return http.build();
    }

    /**
     * 自定义未授权响应
     */
    @Bean
    ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, denied) -> {
            Mono<Void> mono = Mono.defer(() -> Mono.just(exchange.getResponse()))
                    .flatMap(response -> ResponseUtils.writeErrorInfo(response));
            return mono;
        };
    }


    /**
     * 自定义JwtDecoder
     */
    public ReactiveJwtDecoder jwtDecoder() {
        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withPublicKey(generateRsaKey()).build();
        return jwtDecoder;
    }


    /**
     * 其 key 在启动时生成，用于创建上述 JWKSource
     */
    private static RSAPublicKey generateRsaKey() {
        KeyStoreKeyFactory factory = new KeyStoreKeyFactory(new ClassPathResource("demo.jks"), "linmoo".toCharArray());
        KeyPair keyPair = factory.getKeyPair("demo", "linmoo".toCharArray());
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return publicKey;
    }
}
