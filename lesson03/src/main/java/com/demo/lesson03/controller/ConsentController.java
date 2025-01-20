package com.demo.lesson03.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Set;

//是一个Controller，而非RestController
@Controller
public class ConsentController {

    // 注册客户端的Repository
    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @GetMapping(value = "/consent")
    public String consent(Principal principal, Model model,
                          @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
                          @RequestParam(OAuth2ParameterNames.STATE) String state) {

        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        Set<String> scopes = registeredClient.getScopes();

        // 客户端id
        model.addAttribute("clientId", clientId);
        // 客户端名称
        model.addAttribute("clientName", registeredClient.getClientName());
        // state
        model.addAttribute("state", state);
        // 授权范围
        model.addAttribute("scopes", scopes);
        // 认证的用户名
        model.addAttribute("principalName", principal.getName());
        // 回调地址
        model.addAttribute("redirectUri", registeredClient.getRedirectUris().iterator().next());

        // 跳转到consent.html页面
        return "consent";
    }

}

