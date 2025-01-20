package com.demo.lesson06.repository;

import com.demo.lesson06.entity.SelfOAuth2AuthorizationConsent;
import com.demo.lesson06.mapper.OAuth2AuthorizationConsentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeltJdbcOAuth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {

    @Autowired
    private OAuth2AuthorizationConsentMapper auth2AuthorizationConsentMapper;

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Override
    public void save(OAuth2AuthorizationConsent authorizationConsent) {
        Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
        OAuth2AuthorizationConsent existingAuthorizationConsent = findById(authorizationConsent.getRegisteredClientId(),
                authorizationConsent.getPrincipalName());
        if (existingAuthorizationConsent == null) {
            auth2AuthorizationConsentMapper.insert(SelfOAuth2AuthorizationConsent.convertSelfOAuth2AuthorizationConsent(authorizationConsent));
        }
        else {
            auth2AuthorizationConsentMapper.updateById(SelfOAuth2AuthorizationConsent.convertSelfOAuth2AuthorizationConsent(authorizationConsent));
        }
    }

    @Override
    public void remove(OAuth2AuthorizationConsent authorizationConsent) {
        auth2AuthorizationConsentMapper.deleteById(SelfOAuth2AuthorizationConsent.convertSelfOAuth2AuthorizationConsent(authorizationConsent));
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        Map<String, Object> map = new HashMap<>();
        map.put("registered_client_id", registeredClientId);
        map.put("principal_name", principalName);
        List<SelfOAuth2AuthorizationConsent> list = auth2AuthorizationConsentMapper.selectByMap(map);
        return list==null||list.isEmpty()?null:SelfOAuth2AuthorizationConsent.convertOAuth2AuthorizationConsent(list.get(0), registeredClientRepository);
    }

}
