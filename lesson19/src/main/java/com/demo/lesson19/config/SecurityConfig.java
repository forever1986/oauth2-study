package com.demo.lesson19.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        // 本地获取公钥
        http.oauth2ResourceServer(server ->
                server.jwt(jwt->jwt.jwtDecoder(jwtDecoder()))
        );
        // 配置路由拦截
        http.authorizeExchange(exchange->
                        // 设置访问权限
                        exchange.pathMatchers("/demo").hasAuthority("SCOPE_demo"))
                // 关闭csrf
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
        ;

        return http.build();
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
