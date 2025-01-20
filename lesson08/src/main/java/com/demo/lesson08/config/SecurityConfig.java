package com.demo.lesson08.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // userInfo接口需要SCOPE_profile权限
                        .requestMatchers("/userInfo").hasAuthority("SCOPE_profile")
                        // 其它访问都需要鉴权
                        .anyRequest().authenticated()
                )
                // 资源服务器默认配置
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(jwt -> jwt
                        // 配置set-uri替换yaml的jwk-set-uri配置
//                        .jwkSetUri("")
                        // 配置自己的decoder，替换从远程获取的公钥
                        .decoder(jwtDecoder())
                        // 自定义CustomAuthenticationConverter
//                        .jwtAuthenticationConverter(new CustomAuthenticationConverter())
                ));
        return http.build();
    }

    /**
     * 自定义JwtDecoder
     */
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(generateRsaKey()).build();
//        OAuth2TokenValidator<Jwt> withClockSkew = new DelegatingOAuth2TokenValidator<>(
//                new JwtTimestampValidator(Duration.ofSeconds(60)),
//                new JwtIssuerValidator(issuerUri));
//        jwtDecoder.setJwtValidator(withClockSkew);
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
