package com.demo.lesson06.config;

import com.alibaba.fastjson.JSON;
import com.demo.lesson06.redis.RedisSecurityContextRepository;
import com.demo.lesson06.result.Result;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.io.IOException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 授权服务器配置
 */
@Configuration
public class AuthServerConfig {


    @Autowired
    private RedisTemplate redisTemplate;

    // 自定义授权服务器的Filter链
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // oidc配置
                .oidc(withDefaults())
                // 自定义授权页面接口
                .authorizationEndpoint(auth -> auth.consentPage("/consentface"))
        ;
        // 注入Redis获取登录用户信息，因为Spring Security使用的是jwt+redis存储，因此原先基于Session的不可使用
        http.securityContext(c -> c.securityContextRepository(new RedisSecurityContextRepository(redisTemplate)));
        // 资源服务器默认jwt配置
        http.oauth2ResourceServer((resourceServer) -> resourceServer.jwt(withDefaults()));
        // 异常处理
        http.exceptionHandling(exception -> exception.authenticationEntryPoint(new AuthenticationEntryPoint(){
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                authException.printStackTrace();
                Result<String> result = Result.failed("认证失败请重新登录");
                String json = JSON.toJSONString(result);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().println(json);
            }
        }));
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
