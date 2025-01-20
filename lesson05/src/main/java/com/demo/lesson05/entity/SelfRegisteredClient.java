package com.demo.lesson05.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demo.lesson05.handler.ClientSettingsTypeHandler;
import com.demo.lesson05.handler.SetStringTypeHandler;
import com.demo.lesson05.handler.TokenSettingsTypeHandler;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@TableName("oauth2_registered_client")
public class SelfRegisteredClient implements Serializable {

    private String id;

    private String clientId;

    private Instant clientIdIssuedAt;

    private String clientSecret;

    private Instant clientSecretExpiresAt;

    private String clientName;

    @TableField(typeHandler = SetStringTypeHandler.class)
    private Set<String> clientAuthenticationMethods;

    @TableField(typeHandler = SetStringTypeHandler.class)
    private Set<String> authorizationGrantTypes;

    @TableField(typeHandler = SetStringTypeHandler.class)
    private Set<String> redirectUris;

    @TableField(typeHandler = SetStringTypeHandler.class)
    private Set<String> postLogoutRedirectUris;

    @TableField(typeHandler = SetStringTypeHandler.class)
    private Set<String> scopes;

    @TableField(typeHandler = ClientSettingsTypeHandler.class)
    private ClientSettings clientSettings;

    @TableField(typeHandler = TokenSettingsTypeHandler.class)
    private TokenSettings tokenSettings;

    public static RegisteredClient covertRegisteredClient(SelfRegisteredClient selfClient){
        if(selfClient!=null){
            return RegisteredClient
                    .withId(selfClient.getId())
                    .clientId(selfClient.getClientId())
                    .clientSecret(selfClient.getClientSecret())
                    .clientName(selfClient.getClientName())
                    .clientIdIssuedAt(selfClient.getClientIdIssuedAt())
                    .clientSecretExpiresAt(selfClient.getClientSecretExpiresAt())
                    .clientAuthenticationMethods(methods->{
                        methods.addAll(SelfRegisteredClient.getMethodSetFromString(selfClient.getClientAuthenticationMethods()));
                    })
                    .authorizationGrantTypes(types->{
                        types.addAll(SelfRegisteredClient.getSetTypeFromString(selfClient.getAuthorizationGrantTypes()));
                    })
                    .redirectUris(uris->{
                        uris.addAll(selfClient.getRedirectUris());
                    })
                    .postLogoutRedirectUris(uris->{
                        uris.addAll(selfClient.getPostLogoutRedirectUris());
                    })
                    .scopes(scopes1 ->{
                        scopes1.addAll(selfClient.getScopes());
                    })
                    .tokenSettings(selfClient.getTokenSettings())
                    .clientSettings(selfClient.getClientSettings())
                    .build();
        }
        return null;
    }

    public static SelfRegisteredClient covertSelfRegisteredClient(RegisteredClient client){
        if(client!=null){
            SelfRegisteredClient selfRegisteredClient = new SelfRegisteredClient();
            selfRegisteredClient.setId(client.getId());
            selfRegisteredClient.setClientId(client.getClientId());
            selfRegisteredClient.setClientSecret(client.getClientSecret());
            selfRegisteredClient.setClientName(client.getClientName());
            selfRegisteredClient.setClientAuthenticationMethods(getSetFromMethod(client.getClientAuthenticationMethods()));
            selfRegisteredClient.setAuthorizationGrantTypes(getSetFromType(client.getAuthorizationGrantTypes()));
            selfRegisteredClient.setRedirectUris(client.getRedirectUris());
            selfRegisteredClient.setPostLogoutRedirectUris(client.getPostLogoutRedirectUris());
            selfRegisteredClient.setScopes(client.getScopes());
            selfRegisteredClient.setClientSettings(client.getClientSettings());
            selfRegisteredClient.setTokenSettings(client.getTokenSettings());
            selfRegisteredClient.setClientIdIssuedAt(client.getClientIdIssuedAt());
            selfRegisteredClient.setClientSecretExpiresAt(client.getClientSecretExpiresAt());
            return selfRegisteredClient;
        }
        return null;
    }

    public static Set<AuthorizationGrantType> getSetTypeFromString(Set<String> strs){
        Set<AuthorizationGrantType> set = new HashSet<>();
        if(strs!=null&& !strs.isEmpty()){
            // 这里只是用目前OAuth2.1支持的类型，原先的密码就不支持
            for(String authorizationGrantType : strs){
                AuthorizationGrantType type;
                if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
                    type = AuthorizationGrantType.AUTHORIZATION_CODE;
                }
                else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
                    type = AuthorizationGrantType.CLIENT_CREDENTIALS;
                }
                else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
                    type = AuthorizationGrantType.REFRESH_TOKEN;
                }else{
                    // Custom authorization grant type
                    type = new AuthorizationGrantType(authorizationGrantType);
                }
                set.add(type);
            }
        }
        return set;
    }

    public static Set<ClientAuthenticationMethod> getMethodSetFromString(Set<String> strs){
        Set<ClientAuthenticationMethod> set = new HashSet<>();
        if(strs!=null&& !strs.isEmpty()){
            for(String method : strs){
                ClientAuthenticationMethod clientAuthenticationMethod;
                if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equals(method)) {
                    clientAuthenticationMethod = ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
                }
                else if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equals(method)) {
                    clientAuthenticationMethod = ClientAuthenticationMethod.CLIENT_SECRET_POST;
                }
                else if (ClientAuthenticationMethod.NONE.getValue().equals(method)) {
                    clientAuthenticationMethod = ClientAuthenticationMethod.NONE;
                }else {
                    // Custom client authentication method
                    clientAuthenticationMethod = new ClientAuthenticationMethod(method);
                }
                set.add(clientAuthenticationMethod);
            }
        }
        return set;
    }

    public static Set<String> getSetFromType(Set<AuthorizationGrantType> parameters){
        Set<String> set = new HashSet<>();
        if(parameters!=null){
            StringBuilder sb = new StringBuilder();
            for(AuthorizationGrantType parameter : parameters){
                set.add(parameter.getValue());
            }
        }
        return set;
    }

    public static Set<String> getSetFromMethod(Set<ClientAuthenticationMethod> parameters){
        Set<String> set = new HashSet<>();
        if(parameters!=null){
            StringBuilder sb = new StringBuilder();
            for(ClientAuthenticationMethod parameter : parameters){
                set.add(parameter.getValue());
            }
        }
        return set;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Instant getClientIdIssuedAt() {
        return clientIdIssuedAt;
    }

    public void setClientIdIssuedAt(Instant clientIdIssuedAt) {
        this.clientIdIssuedAt = clientIdIssuedAt;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Instant getClientSecretExpiresAt() {
        return clientSecretExpiresAt;
    }

    public void setClientSecretExpiresAt(Instant clientSecretExpiresAt) {
        this.clientSecretExpiresAt = clientSecretExpiresAt;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Set<String> getClientAuthenticationMethods() {
        return clientAuthenticationMethods;
    }

    public void setClientAuthenticationMethods(Set<String> clientAuthenticationMethods) {
        this.clientAuthenticationMethods = clientAuthenticationMethods;
    }

    public Set<String> getAuthorizationGrantTypes() {
        return authorizationGrantTypes;
    }

    public void setAuthorizationGrantTypes(Set<String> authorizationGrantTypes) {
        this.authorizationGrantTypes = authorizationGrantTypes;
    }

    public Set<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(Set<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public Set<String> getPostLogoutRedirectUris() {
        return postLogoutRedirectUris;
    }

    public void setPostLogoutRedirectUris(Set<String> postLogoutRedirectUris) {
        this.postLogoutRedirectUris = postLogoutRedirectUris;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public ClientSettings getClientSettings() {
        return clientSettings;
    }

    public void setClientSettings(ClientSettings clientSettings) {
        this.clientSettings = clientSettings;
    }

    public TokenSettings getTokenSettings() {
        return tokenSettings;
    }

    public void setTokenSettings(TokenSettings tokenSettings) {
        this.tokenSettings = tokenSettings;
    }
}
