package com.demo.lesson04.controller;

import com.demo.lesson04.entity.SelfRegisteredClient;
import com.demo.lesson04.mapper.Oauth2RegisteredClientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
public class InsertController {

    @Autowired
    Oauth2RegisteredClientMapper oauth2RegisteredClientMapper;

    @Autowired
    RegisteredClientRepository registeredClientRepository;

    @GetMapping("/insert")
    public void insert(){
        SelfRegisteredClient client = new SelfRegisteredClient();
        client.setId(UUID.randomUUID().toString());
        client.setClientId("oidc-client");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        client.setClientSecret("{bcrypt}"+ encoder.encode("secret"));
        client.setClientName("oidc-client");
        Set<ClientAuthenticationMethod> methodSet = new HashSet<>();
        client.setClientAuthenticationMethods(new HashSet<>(Collections.singleton(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue())));
        Set<String> typeSet = new HashSet<>();
        typeSet.add(AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
        typeSet.add(AuthorizationGrantType.REFRESH_TOKEN.getValue());
        client.setAuthorizationGrantTypes(typeSet);
        client.setRedirectUris(new HashSet<>(Collections.singleton("http://localhost:8080/login/oauth2/code/oidc-client")));
        client.setPostLogoutRedirectUris(new HashSet<>(Collections.singleton("http://localhost:8080/")));
        Set<String> scopeSet = new HashSet<>();
        scopeSet.add(OidcScopes.OPENID);
        scopeSet.add(OidcScopes.PROFILE);
        client.setScopes(scopeSet);
        client.setClientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build());
        client.setTokenSettings(TokenSettings.builder().build());

        oauth2RegisteredClientMapper.insert(client);
        System.out.println(client);
    }

    @GetMapping("/save")
    public void save(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        RegisteredClient client = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("oidc-client")
                .clientSecret("{bcrypt}"+ encoder.encode("secret"))
                .clientName("oidc-client")
                .clientAuthenticationMethods(methods -> methods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
                .authorizationGrantTypes(types -> {
                    types.add(AuthorizationGrantType.AUTHORIZATION_CODE);
                    types.add(AuthorizationGrantType.REFRESH_TOKEN);
                })
                .redirectUri("http://localhost:8080/login/oauth2/code/oidc-client")
                .postLogoutRedirectUri("http://localhost:8080/")
                .scopes(scopes -> {
                    scopes.add(OidcScopes.OPENID);
                    scopes.add(OidcScopes.PROFILE);
                })
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .tokenSettings(TokenSettings.builder().build())
                .build();

        registeredClientRepository.save(client);
        System.out.println(client);
    }
}
