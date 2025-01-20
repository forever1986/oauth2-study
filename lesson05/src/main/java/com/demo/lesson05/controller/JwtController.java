package com.demo.lesson05.controller;

import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Writer;

@RestController
public class JwtController {


    @Autowired
    private JWKSource<SecurityContext> jwkSource;


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
}
