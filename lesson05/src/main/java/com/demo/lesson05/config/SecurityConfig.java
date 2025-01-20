package com.demo.lesson05.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    // 自定义授权服务器的Filter链
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // oidc配置
                .oidc(withDefaults())
        ;
        // 资源服务器默认jwt配置
        http.oauth2ResourceServer((resourceServer) -> resourceServer.jwt(withDefaults()));
        // 异常处理
        http.exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(
                new LoginUrlAuthenticationEntryPoint("/login")));
        return http.build();
    }

    // 自定义Spring Security的链路。如果自定义授权服务器的Filter链，则原先自动化配置将会失效，因此也要配置Spring Security
    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorize) -> authorize
                // 该接口开放Jwks数据，无需权限，用于资源服务器自定义获取Jwks
                .requestMatchers("/jwt").permitAll()
                .anyRequest().authenticated()).formLogin(withDefaults());
        return http.build();
    }

    /**
     * 访问令牌签名
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * 其 key 在启动时生成，用于创建上述 JWKSource
     */
    private static KeyPair generateRsaKey() {
        KeyStoreKeyFactory factory = new KeyStoreKeyFactory(new ClassPathResource("demo.jks"), "linmoo".toCharArray());
        KeyPair keyPair = factory.getKeyPair("demo", "linmoo".toCharArray());
        return keyPair;
    }
}
