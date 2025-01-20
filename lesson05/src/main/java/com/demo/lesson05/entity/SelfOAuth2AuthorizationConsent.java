package com.demo.lesson05.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demo.lesson05.handler.SetStringTypeHandler;
import lombok.Data;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

@TableName("oauth2_authorization_consent")
@Data
public class SelfOAuth2AuthorizationConsent implements Serializable {

    private String registeredClientId;

    private String principalName;

    @TableField(typeHandler = SetStringTypeHandler.class)
    private Set<String> authorities;


    public static SelfOAuth2AuthorizationConsent convertSelfOAuth2AuthorizationConsent(OAuth2AuthorizationConsent auth2AuthorizationConsent){
        if(auth2AuthorizationConsent!=null){
            SelfOAuth2AuthorizationConsent selfOAuth2AuthorizationConsent = new SelfOAuth2AuthorizationConsent();
            selfOAuth2AuthorizationConsent.setRegisteredClientId(auth2AuthorizationConsent.getRegisteredClientId());
            selfOAuth2AuthorizationConsent.setPrincipalName(auth2AuthorizationConsent.getPrincipalName());
            if(auth2AuthorizationConsent.getAuthorities()!=null){
                selfOAuth2AuthorizationConsent.setAuthorities(auth2AuthorizationConsent.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
            }
            return selfOAuth2AuthorizationConsent;
        }
        return null;
    }

    public static OAuth2AuthorizationConsent convertOAuth2AuthorizationConsent(SelfOAuth2AuthorizationConsent selfOAuth2AuthorizationConsent, RegisteredClientRepository registeredClientRepository){
        if(selfOAuth2AuthorizationConsent!=null){
            RegisteredClient registeredClient = registeredClientRepository.findById(selfOAuth2AuthorizationConsent.getRegisteredClientId());
            if (registeredClient == null) {
                throw new DataRetrievalFailureException("The RegisteredClient with id '" + selfOAuth2AuthorizationConsent.getRegisteredClientId()
                        + "' was not found in the RegisteredClientRepository.");
            }
            OAuth2AuthorizationConsent.Builder builder = OAuth2AuthorizationConsent.withId(selfOAuth2AuthorizationConsent.getRegisteredClientId(),
                    selfOAuth2AuthorizationConsent.getPrincipalName());
            for (String authority : selfOAuth2AuthorizationConsent.getAuthorities()) {
                builder.authority(new SimpleGrantedAuthority(authority));
            }
            return builder.build();
        }
        return null;
    }
}
