package com.demo.lesson06.controller;

import com.demo.lesson06.dto.ConsentDTO;
import com.demo.lesson06.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Set;

//自定义授权页面接口，返回state数据，不返回页面，由前端去组装页面
@RestController
public class ConsentController {

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @GetMapping(value = "/consentface")
    public Result<ConsentDTO> consent(Principal principal, Model model,
                                  @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
                                  @RequestParam(OAuth2ParameterNames.STATE) String state) {

        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        Set<String> scopes = registeredClient.getScopes();
        ConsentDTO consentDTO = new ConsentDTO();
        consentDTO.setClientId(clientId);
        consentDTO.setClientName(registeredClient.getClientName());
        consentDTO.setState(state);
        consentDTO.setScopes(scopes);
        consentDTO.setPrincipalName(principal.getName());
        consentDTO.setRedirectUri(registeredClient.getRedirectUris().iterator().next());
        return Result.success(consentDTO);
    }

}

