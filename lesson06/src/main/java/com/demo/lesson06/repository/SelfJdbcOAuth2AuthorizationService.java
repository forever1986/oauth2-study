package com.demo.lesson06.repository;

import com.demo.lesson06.entity.SelfOAuth2Authorization;
import com.demo.lesson06.mapper.OAuth2AuthorizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SelfJdbcOAuth2AuthorizationService implements OAuth2AuthorizationService {

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Autowired
    private OAuth2AuthorizationMapper oAuth2AuthorizationMapper;

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        OAuth2Authorization existingAuthorization = findById(authorization.getId());
        if (existingAuthorization == null) {
            oAuth2AuthorizationMapper.insert(SelfOAuth2Authorization.covertSelfOAuth2Authorization(authorization));
        }
        else {
            oAuth2AuthorizationMapper.updateById(SelfOAuth2Authorization.covertSelfOAuth2Authorization(authorization));
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        oAuth2AuthorizationMapper.deleteById(SelfOAuth2Authorization.covertSelfOAuth2Authorization(authorization));
    }

    @Override
    public OAuth2Authorization findById(String id) {
        SelfOAuth2Authorization selfOAuth2Authorization = oAuth2AuthorizationMapper.selectById(id);
        return SelfOAuth2Authorization.covertOAuth2Authorization(selfOAuth2Authorization, registeredClientRepository);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        List<SqlParameterValue> parameters = new ArrayList<>();
        List<SelfOAuth2Authorization> result = null;
        Map<String, Object> map = new HashMap<>();
        if (tokenType == null) {
            map.put("state", token);
            byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
            map.put("authorization_code_value", tokenBytes);
            map.put("access_token_value", tokenBytes);
            map.put("oidc_id_token_value", tokenBytes);
            map.put("refresh_token_value", tokenBytes);
            map.put("user_code_value", tokenBytes);
            map.put("device_code_value", tokenBytes);
            result = oAuth2AuthorizationMapper.selectByMap(map);
        }
        else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
            map.put("state", token);
            result = oAuth2AuthorizationMapper.selectByMap(map);
        }
        else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
            map.put("authorization_code_value", token.getBytes(StandardCharsets.UTF_8));
            result = oAuth2AuthorizationMapper.selectByMap(map);
        }
        else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            map.put("access_token_value", token.getBytes(StandardCharsets.UTF_8));
            result = oAuth2AuthorizationMapper.selectByMap(map);
        }
        else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
            map.put("oidc_id_token_value", token.getBytes(StandardCharsets.UTF_8));
            result = oAuth2AuthorizationMapper.selectByMap(map);
        }
        else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            map.put("refresh_token_value", token.getBytes(StandardCharsets.UTF_8));
            result = oAuth2AuthorizationMapper.selectByMap(map);
        }
        else if (OAuth2ParameterNames.USER_CODE.equals(tokenType.getValue())) {
            map.put("user_code_value", token.getBytes(StandardCharsets.UTF_8));
            result = oAuth2AuthorizationMapper.selectByMap(map);
        }
        else if (OAuth2ParameterNames.DEVICE_CODE.equals(tokenType.getValue())) {
            map.put("device_code_value", token.getBytes(StandardCharsets.UTF_8));
            result = oAuth2AuthorizationMapper.selectByMap(map);
        }
        return result!=null&&!result.isEmpty()?SelfOAuth2Authorization.covertOAuth2Authorization(result.get(0),registeredClientRepository):null;
    }
}
