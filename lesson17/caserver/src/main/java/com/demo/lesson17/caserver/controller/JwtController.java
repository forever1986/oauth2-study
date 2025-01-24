package com.demo.lesson17.caserver.controller;

import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class JwtController {


    private JWKSource<SecurityContext> jwkSource = jwkSource();


    @GetMapping("/jwt")
    public String jwt(){
        JWKSet jwkSet;
        try {
            JWKSelector jwkSelector = new JWKSelector(new JWKMatcher.Builder().build());
            jwkSet = new JWKSet(this.jwkSource.get(jwkSelector, null));
        }
        catch (Exception ex) {
            throw new IllegalStateException("Failed to select the JWK(s) -> " + ex.getMessage(), ex);
        }

        return jwkSet.toString();
    }


    /**
     * 生成JWKSource
     */
    private static JWKSource<SecurityContext> jwkSource(){
        X509Certificate certificate = null;
        List<Base64> base64List = new ArrayList<>();
        try {
            certificate = getCertificate();
            base64List.add(Base64.encode(certificate.getEncoded()));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        RSAKey rsaKey = new RSAKey.Builder( (RSAPublicKey)certificate.getPublicKey())
                .keyID(UUID.randomUUID().toString())
                .x509CertChain(base64List)
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * 读取证书
     */
    private static X509Certificate getCertificate() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new ClassPathResource("oidc-client.jks").getInputStream(),"linmoo".toCharArray());
        Certificate[] list = ks.getCertificateChain("oidc-client");
        return (X509Certificate)list[0];
    }
}
