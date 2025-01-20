package com.demo.lesson04.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demo.lesson04.handler.SetStringTypeHandler;
import com.demo.lesson04.handler.TokenMetadataTypeHandler;
import lombok.Data;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@TableName("oauth2_authorization")
@Data
public class SelfOAuth2Authorization implements Serializable {

    private String id;

    private String registeredClientId;

    private String principalName;

    private String authorizationGrantType;

    @TableField(typeHandler = SetStringTypeHandler.class)
    private Set<String> authorizedScopes;

    @TableField(typeHandler = TokenMetadataTypeHandler.class)
    private Map<String, Object> attributes;

    private String state;

    private String authorizationCodeValue;

    private Timestamp authorizationCodeIssuedAt;

    private Timestamp authorizationCodeExpiresAt;

    @TableField(typeHandler = TokenMetadataTypeHandler.class)
    private Map<String, Object> authorizationCodeMetadata;

    private String accessTokenValue;

    private Timestamp accessTokenIssuedAt;

    private Timestamp accessTokenExpiresAt;

    @TableField(typeHandler = TokenMetadataTypeHandler.class)
    private Map<String, Object> accessTokenMetadata;

    private String  accessTokenType;

    @TableField(typeHandler = SetStringTypeHandler.class)
    private Set<String>  accessTokenScopes;

    private String oidcIdTokenValue;

    private Timestamp oidcIdTokenIssuedAt;

    private Timestamp oidcIdTokenExpiresAt;

    @TableField(typeHandler = TokenMetadataTypeHandler.class)
    private Map<String, Object> oidcIdTokenMetadata;

    private String refreshTokenValue;

    private Timestamp refreshTokenIssuedAt;

    private Timestamp refreshTokenExpiresAt;

    @TableField(typeHandler = TokenMetadataTypeHandler.class)
    private Map<String, Object> refreshTokenMetadata;

    private String userCodeValue;

    private Timestamp userCodeIssuedAt;

    private Timestamp userCodeExpiresAt;

    @TableField(typeHandler = TokenMetadataTypeHandler.class)
    private Map<String, Object> userCodeMetadata;

    private String deviceCodeValue;

    private Timestamp deviceCodeIssuedAt;

    private Timestamp deviceCodeExpiresAt;

    @TableField(typeHandler = TokenMetadataTypeHandler.class)
    private Map<String, Object> deviceCodeMetadata;


    public static OAuth2Authorization covertOAuth2Authorization(SelfOAuth2Authorization selfOAuth2Authorization, RegisteredClientRepository registeredClientRepository){
        if(selfOAuth2Authorization!=null){
            RegisteredClient registeredClient = registeredClientRepository.findById(selfOAuth2Authorization.getRegisteredClientId());
            if (registeredClient == null) {
                throw new DataRetrievalFailureException("The RegisteredClient with id '" + selfOAuth2Authorization.getRegisteredClientId()
                        + "' was not found in the RegisteredClientRepository.");
            }

            OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient);
            builder.id(selfOAuth2Authorization.getId())
                    .principalName(selfOAuth2Authorization.getPrincipalName())
                    .authorizationGrantType(new AuthorizationGrantType(selfOAuth2Authorization.getAuthorizationGrantType()))
                    .authorizedScopes(selfOAuth2Authorization.getAuthorizedScopes())
                    .attributes((attrs) -> attrs.putAll(selfOAuth2Authorization.getAttributes()));

            String state = selfOAuth2Authorization.getState();
            if (StringUtils.hasText(state)) {
                builder.attribute(OAuth2ParameterNames.STATE, state);
            }

            Instant tokenIssuedAt;
            Instant tokenExpiresAt;
            String authorizationCodeValue = selfOAuth2Authorization.getAuthorizationCodeValue();

            if (StringUtils.hasText(authorizationCodeValue)) {
                tokenIssuedAt = selfOAuth2Authorization.getAuthorizationCodeIssuedAt().toInstant();
                tokenExpiresAt = selfOAuth2Authorization.getAuthorizationCodeExpiresAt().toInstant();
                Map<String, Object> authorizationCodeMetadata = selfOAuth2Authorization.getAuthorizationCodeMetadata();

                OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(authorizationCodeValue,
                        tokenIssuedAt, tokenExpiresAt);
                builder.token(authorizationCode, (metadata) -> metadata.putAll(authorizationCodeMetadata));
            }

            String accessTokenValue = selfOAuth2Authorization.getAccessTokenValue();
            if (StringUtils.hasText(accessTokenValue)) {
                tokenIssuedAt = selfOAuth2Authorization.getAccessTokenIssuedAt().toInstant();
                tokenExpiresAt = selfOAuth2Authorization.getAccessTokenExpiresAt().toInstant();
                Map<String, Object> accessTokenMetadata = selfOAuth2Authorization.getAccessTokenMetadata();
                OAuth2AccessToken.TokenType tokenType = null;
                if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(selfOAuth2Authorization.getAccessTokenType())) {
                    tokenType = OAuth2AccessToken.TokenType.BEARER;
                }

                Set<String> scopes = selfOAuth2Authorization.getAccessTokenScopes();
                OAuth2AccessToken accessToken = new OAuth2AccessToken(tokenType, accessTokenValue, tokenIssuedAt,
                        tokenExpiresAt, scopes);
                builder.token(accessToken, (metadata) -> metadata.putAll(accessTokenMetadata));
            }

            String oidcIdTokenValue = selfOAuth2Authorization.getOidcIdTokenValue();
            if (StringUtils.hasText(oidcIdTokenValue)) {
                tokenIssuedAt = selfOAuth2Authorization.getOidcIdTokenIssuedAt().toInstant();
                tokenExpiresAt = selfOAuth2Authorization.getOidcIdTokenExpiresAt().toInstant();
                Map<String, Object> oidcTokenMetadata = selfOAuth2Authorization.getOidcIdTokenMetadata();

                OidcIdToken oidcToken = new OidcIdToken(oidcIdTokenValue, tokenIssuedAt, tokenExpiresAt,
                        (Map<String, Object>) oidcTokenMetadata.get(OAuth2Authorization.Token.CLAIMS_METADATA_NAME));
                builder.token(oidcToken, (metadata) -> metadata.putAll(oidcTokenMetadata));
            }

            String refreshTokenValue = selfOAuth2Authorization.getRefreshTokenValue();
            if (StringUtils.hasText(refreshTokenValue)) {
                tokenIssuedAt = selfOAuth2Authorization.getRefreshTokenIssuedAt().toInstant();
                tokenExpiresAt = null;
                Timestamp refreshTokenExpiresAt = selfOAuth2Authorization.getRefreshTokenExpiresAt();
                if (refreshTokenExpiresAt != null) {
                    tokenExpiresAt = refreshTokenExpiresAt.toInstant();
                }
                Map<String, Object> refreshTokenMetadata = selfOAuth2Authorization.getRefreshTokenMetadata();

                OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(refreshTokenValue, tokenIssuedAt,
                        tokenExpiresAt);
                builder.token(refreshToken, (metadata) -> metadata.putAll(refreshTokenMetadata));
            }

            String userCodeValue = selfOAuth2Authorization.getUserCodeValue();
            if (StringUtils.hasText(userCodeValue)) {
                tokenIssuedAt = selfOAuth2Authorization.getUserCodeIssuedAt().toInstant();
                tokenExpiresAt = selfOAuth2Authorization.getUserCodeExpiresAt().toInstant();
                Map<String, Object> userCodeMetadata = selfOAuth2Authorization.getUserCodeMetadata();

                OAuth2UserCode userCode = new OAuth2UserCode(userCodeValue, tokenIssuedAt, tokenExpiresAt);
                builder.token(userCode, (metadata) -> metadata.putAll(userCodeMetadata));
            }

            String deviceCodeValue = selfOAuth2Authorization.getDeviceCodeValue();
            if (StringUtils.hasText(deviceCodeValue)) {
                tokenIssuedAt = selfOAuth2Authorization.getDeviceCodeIssuedAt().toInstant();
                tokenExpiresAt = selfOAuth2Authorization.getDeviceCodeExpiresAt().toInstant();
                Map<String, Object> deviceCodeMetadata = selfOAuth2Authorization.getDeviceCodeMetadata();

                OAuth2DeviceCode deviceCode = new OAuth2DeviceCode(deviceCodeValue, tokenIssuedAt, tokenExpiresAt);
                builder.token(deviceCode, (metadata) -> metadata.putAll(deviceCodeMetadata));
            }
            return builder.build();
        }
        return null;
    }

    public static SelfOAuth2Authorization covertSelfOAuth2Authorization(OAuth2Authorization auth2Authorization){
        if(auth2Authorization!=null){

            SelfOAuth2Authorization selfOAuth2Authorization = new SelfOAuth2Authorization();
            selfOAuth2Authorization.setId(auth2Authorization.getId());
            selfOAuth2Authorization.setRegisteredClientId(auth2Authorization.getRegisteredClientId());
            selfOAuth2Authorization.setPrincipalName(auth2Authorization.getPrincipalName());
            selfOAuth2Authorization.setAuthorizationGrantType(auth2Authorization.getAuthorizationGrantType().getValue());

            selfOAuth2Authorization.setAuthorizedScopes(auth2Authorization.getAuthorizedScopes());

            selfOAuth2Authorization.setAttributes(auth2Authorization.getAttributes());

            String state = null;
            String authorizationState = auth2Authorization.getAttribute(OAuth2ParameterNames.STATE);
            if (StringUtils.hasText(authorizationState)) {
                state = authorizationState;
            }
            selfOAuth2Authorization.setState(state==null?"":state);

            OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode = auth2Authorization
                    .getToken(OAuth2AuthorizationCode.class);
            if(authorizationCode!=null){
                selfOAuth2Authorization.setAuthorizationCodeValue(authorizationCode.getToken().getTokenValue());
                selfOAuth2Authorization.setAuthorizationCodeIssuedAt(new Timestamp(authorizationCode.getToken().getIssuedAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setAuthorizationCodeExpiresAt(new Timestamp(authorizationCode.getToken().getExpiresAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setAuthorizationCodeMetadata(authorizationCode.getMetadata());
            }

            OAuth2Authorization.Token<OAuth2AccessToken> accessToken = auth2Authorization.getToken(OAuth2AccessToken.class);
            if (accessToken != null) {
                selfOAuth2Authorization.setAccessTokenValue(accessToken.getToken().getTokenValue());
                selfOAuth2Authorization.setAccessTokenIssuedAt(new Timestamp(accessToken.getToken().getIssuedAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setAccessTokenExpiresAt(new Timestamp(accessToken.getToken().getExpiresAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setAccessTokenMetadata(accessToken.getMetadata());
                selfOAuth2Authorization.setAccessTokenType(accessToken.getToken().getTokenType().getValue());
                selfOAuth2Authorization.setAccessTokenScopes(accessToken.getToken().getScopes());
            }

            OAuth2Authorization.Token<OidcIdToken> oidcIdToken = auth2Authorization.getToken(OidcIdToken.class);
            if (oidcIdToken != null) {
                selfOAuth2Authorization.setOidcIdTokenValue(oidcIdToken.getToken().getTokenValue());
                selfOAuth2Authorization.setOidcIdTokenIssuedAt(new Timestamp(oidcIdToken.getToken().getIssuedAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setOidcIdTokenExpiresAt(new Timestamp(oidcIdToken.getToken().getExpiresAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setOidcIdTokenMetadata(oidcIdToken.getMetadata());
            }

            OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = auth2Authorization.getRefreshToken();
            if (refreshToken != null) {
                selfOAuth2Authorization.setRefreshTokenValue(refreshToken.getToken().getTokenValue());
                selfOAuth2Authorization.setRefreshTokenIssuedAt(new Timestamp(refreshToken.getToken().getIssuedAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setRefreshTokenExpiresAt(new Timestamp(refreshToken.getToken().getExpiresAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setRefreshTokenMetadata(refreshToken.getMetadata());
            }

            OAuth2Authorization.Token<OAuth2UserCode> userCode = auth2Authorization.getToken(OAuth2UserCode.class);
            if (userCode != null) {
                selfOAuth2Authorization.setUserCodeValue(userCode.getToken().getTokenValue());
                selfOAuth2Authorization.setUserCodeIssuedAt(new Timestamp(userCode.getToken().getIssuedAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setUserCodeExpiresAt(new Timestamp(userCode.getToken().getExpiresAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setUserCodeMetadata(userCode.getMetadata());
            }

            OAuth2Authorization.Token<OAuth2DeviceCode> deviceCode = auth2Authorization.getToken(OAuth2DeviceCode.class);
            if (deviceCode != null) {
                selfOAuth2Authorization.setDeviceCodeValue(deviceCode.getToken().getTokenValue());
                selfOAuth2Authorization.setDeviceCodeIssuedAt(new Timestamp(deviceCode.getToken().getIssuedAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setDeviceCodeExpiresAt(new Timestamp(deviceCode.getToken().getExpiresAt().getEpochSecond()*1000));
                selfOAuth2Authorization.setDeviceCodeMetadata(deviceCode.getMetadata());
            }

            return selfOAuth2Authorization;
        }
        return null;
    }
}
