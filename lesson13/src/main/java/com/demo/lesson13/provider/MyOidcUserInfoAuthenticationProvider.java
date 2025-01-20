package com.demo.lesson13.provider;

import com.demo.lesson13.entity.TUser;
import com.demo.lesson13.mapper.TUserMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MyOidcUserInfoAuthenticationProvider implements AuthenticationProvider {

    private final Log logger = LogFactory.getLog(getClass());

    private final OAuth2AuthorizationService authorizationService;

    private final TUserMapper tUserMapper;

    private DefaultOidcUserInfoMapper userInfoMapper = new MyOidcUserInfoAuthenticationProvider.DefaultOidcUserInfoMapper();


    /**
     * Constructs an {@code OidcUserInfoAuthenticationProvider} using the provided
     * parameters.
     * @param authorizationService the authorization service
     */
    public MyOidcUserInfoAuthenticationProvider(OAuth2AuthorizationService authorizationService, TUserMapper tUserMapper) {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tUserMapper, "tUserMapper cannot be null");
        this.authorizationService = authorizationService;
        this.tUserMapper = tUserMapper;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OidcUserInfoAuthenticationToken userInfoAuthentication = (OidcUserInfoAuthenticationToken) authentication;

        AbstractOAuth2TokenAuthenticationToken<?> accessTokenAuthentication = null;
        if (AbstractOAuth2TokenAuthenticationToken.class
                .isAssignableFrom(userInfoAuthentication.getPrincipal().getClass())) {
            accessTokenAuthentication = (AbstractOAuth2TokenAuthenticationToken<?>) userInfoAuthentication
                    .getPrincipal();
        }
        if (accessTokenAuthentication == null || !accessTokenAuthentication.isAuthenticated()) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }

        String accessTokenValue = accessTokenAuthentication.getToken().getTokenValue();

        OAuth2Authorization authorization = this.authorizationService.findByToken(accessTokenValue,
                OAuth2TokenType.ACCESS_TOKEN);
        if (authorization == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Retrieved authorization with access token");
        }

        OAuth2Authorization.Token<OAuth2AccessToken> authorizedAccessToken = authorization.getAccessToken();
        if (!authorizedAccessToken.isActive()) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }

        if (!authorizedAccessToken.getToken().getScopes().contains(OidcScopes.OPENID)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INSUFFICIENT_SCOPE);
        }

        OAuth2Authorization.Token<OidcIdToken> idToken = authorization.getToken(OidcIdToken.class);
        if (idToken == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }
        // 修改1：此处从数据库获得数据，并塞入idToken即可
        TUser tUser = null;
        if(idToken.getClaims()!=null&&idToken.getClaims().get(StandardClaimNames.SUB) instanceof String){
            tUser = tUserMapper.selectByUsername((String) idToken.getClaims().get(StandardClaimNames.SUB));
        }
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Validated user info request");
        }
        OidcUserInfoAuthenticationContext authenticationContext = OidcUserInfoAuthenticationContext
                .with(userInfoAuthentication)
                .accessToken(authorizedAccessToken.getToken())
                .authorization(authorization)
                .build();
        // 修改2：传入tUser
        OidcUserInfo userInfo = this.userInfoMapper.apply(authenticationContext, tUser);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Authenticated user info request");
        }

        return new OidcUserInfoAuthenticationToken(accessTokenAuthentication, userInfo);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OidcUserInfoAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * Sets the {@link Function} used to extract claims from
     * {@link OidcUserInfoAuthenticationContext} to an instance of {@link OidcUserInfo}
     * for the UserInfo response.
     *
     * <p>
     * The {@link OidcUserInfoAuthenticationContext} gives the mapper access to the
     * {@link OidcUserInfoAuthenticationToken}, as well as, the following context
     * attributes:
     * <ul>
     * <li>{@link OidcUserInfoAuthenticationContext#getAccessToken()} containing the
     * bearer token used to make the request.</li>
     * <li>{@link OidcUserInfoAuthenticationContext#getAuthorization()} containing the
     * {@link OidcIdToken} and {@link OAuth2AccessToken} associated with the bearer token
     * used to make the request.</li>
     * </ul>
     * @param userInfoMapper the {@link Function} used to extract claims from
     * {@link OidcUserInfoAuthenticationContext} to an instance of {@link OidcUserInfo}
     */
    public void setUserInfoMapper(DefaultOidcUserInfoMapper userInfoMapper) {
        Assert.notNull(userInfoMapper, "userInfoMapper cannot be null");
        this.userInfoMapper = userInfoMapper;
    }

    private static final class DefaultOidcUserInfoMapper {

        // @formatter:off
        private static final List<String> EMAIL_CLAIMS = Arrays.asList(
                StandardClaimNames.EMAIL,
                StandardClaimNames.EMAIL_VERIFIED
        );
        private static final List<String> PHONE_CLAIMS = Arrays.asList(
                StandardClaimNames.PHONE_NUMBER,
                StandardClaimNames.PHONE_NUMBER_VERIFIED
        );
        private static final List<String> PROFILE_CLAIMS = Arrays.asList(
                StandardClaimNames.NAME,
                StandardClaimNames.FAMILY_NAME,
                StandardClaimNames.GIVEN_NAME,
                StandardClaimNames.MIDDLE_NAME,
                StandardClaimNames.NICKNAME,
                StandardClaimNames.PREFERRED_USERNAME,
                StandardClaimNames.PROFILE,
                StandardClaimNames.PICTURE,
                StandardClaimNames.WEBSITE,
                StandardClaimNames.GENDER,
                StandardClaimNames.BIRTHDATE,
                StandardClaimNames.ZONEINFO,
                StandardClaimNames.LOCALE,
                StandardClaimNames.UPDATED_AT
        );
        // @formatter:on

        public OidcUserInfo apply(OidcUserInfoAuthenticationContext authenticationContext, TUser tUser) {
            OAuth2Authorization authorization = authenticationContext.getAuthorization();
            OidcIdToken idToken = authorization.getToken(OidcIdToken.class).getToken();
            // 修改3：组装新的map,为了演示，我们只需要传入电话和email做演示
            Map<String, Object> map = new ConcurrentHashMap<>(idToken.getClaims());
            map.put(StandardClaimNames.EMAIL, tUser.getEmail());
            map.put(StandardClaimNames.PHONE_NUMBER, tUser.getPhone());
            OAuth2AccessToken accessToken = authenticationContext.getAccessToken();
            Map<String, Object> scopeRequestedClaims = getClaimsRequestedByScope(map,
                    accessToken.getScopes());

            return new OidcUserInfo(scopeRequestedClaims);
        }

        private static Map<String, Object> getClaimsRequestedByScope(Map<String, Object> claims,
                                                                     Set<String> requestedScopes) {
            Set<String> scopeRequestedClaimNames = new HashSet<>(32);
            scopeRequestedClaimNames.add(StandardClaimNames.SUB);

            if (requestedScopes.contains(OidcScopes.ADDRESS)) {
                scopeRequestedClaimNames.add(StandardClaimNames.ADDRESS);
            }
            if (requestedScopes.contains(OidcScopes.EMAIL)) {
                scopeRequestedClaimNames.addAll(EMAIL_CLAIMS);
            }
            if (requestedScopes.contains(OidcScopes.PHONE)) {
                scopeRequestedClaimNames.addAll(PHONE_CLAIMS);
            }
            if (requestedScopes.contains(OidcScopes.PROFILE)) {
                scopeRequestedClaimNames.addAll(PROFILE_CLAIMS);
            }

            Map<String, Object> requestedClaims = new HashMap<>(claims);
            requestedClaims.keySet().removeIf(claimName -> !scopeRequestedClaimNames.contains(claimName));

            return requestedClaims;
        }

    }

}
