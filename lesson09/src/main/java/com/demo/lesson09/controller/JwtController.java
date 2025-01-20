package com.demo.lesson09.controller;

import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@RestController
public class JwtController {


    /**
     * 返回公钥
     * @return
     */
    @GetMapping("/jwt")
    public String jwt(){
        JWKSet jwkSet;
        try {
            JWKSelector jwkSelector = new JWKSelector(new JWKMatcher.Builder().build());
            jwkSet = new JWKSet(jwkSource().get(jwkSelector, null));
        }
        catch (Exception ex) {
            throw new IllegalStateException("Failed to select the JWK(s) -> " + ex.getMessage(), ex);
        }

        return jwkSet.toString();
    }


    private static String UUID_STR = "b07b297d-a1e3-4a86-a8fe-632ebd61545b";

    // Jwt加密
    private static JwtEncoder jwtEncoder() {
        NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource());
        return jwtEncoder;
    }

    // Jwt解密
    private static JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey)generateRsaKey().getPublic()).build();
        return jwtDecoder;
    }

    // 获取JWKSource
    private static JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID_STR)
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    // 获取本地的demo.jks
    private static KeyPair generateRsaKey() {
        KeyStoreKeyFactory factory = new KeyStoreKeyFactory(new ClassPathResource("demo.jks"), "linmoo".toCharArray());
        KeyPair keyPair = factory.getKeyPair("demo", "linmoo".toCharArray());
        return keyPair;
    }


    public static void main(String[] args) {
        String clientId = "oidc-client";
        // 至少以下四项信息
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
        claimsBuilder
                // 发布者
                .issuer(clientId)
                // 对象
                .subject(clientId)
                // 授权服务器地址
                .audience(Collections.singletonList("http://localhost:9000"))
                // 发布时间
                .issuedAt((new Timestamp(System.currentTimeMillis())).toInstant())
                // 过期时间
                .expiresAt((new Date(System.currentTimeMillis() + 1000 * 60 * 10)).toInstant())
                .id(UUID.randomUUID().toString());
        JwsHeader.Builder jwsHeaderBuilder = JwsHeader.with(SignatureAlgorithm.RS256);

        JwsHeader jwsHeader = jwsHeaderBuilder.build();
        JwtClaimsSet claims = claimsBuilder.build();
        Jwt jwt = jwtEncoder().encode(JwtEncoderParameters.from(jwsHeader, claims));
        String token = jwt.getTokenValue();
        System.out.println(token);
        Jwt newjwt =jwtDecoder().decode(token);
        System.out.println(newjwt);
    }
}
