package com.demo.lesson01.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2LoginController {

    // http://localhost:8080/oauthlogin
    @GetMapping("/oauthlogin")
    public String oauthlogin(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
                             @AuthenticationPrincipal OAuth2User oauth2User) {
        // 这里我们取出用户名，并返回
        return oauth2User.getName();
    }
}
