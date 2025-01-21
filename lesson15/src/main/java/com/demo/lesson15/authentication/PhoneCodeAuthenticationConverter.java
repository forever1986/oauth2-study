package com.demo.lesson15.authentication;

import com.demo.lesson15.constant.MyAuthorizationGrantType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 自定义AuthenticationConverter
 */
public class PhoneCodeAuthenticationConverter implements AuthenticationConverter {

    public static final String PHONE_NUM = "phone_num";

    public static final String PHONE_CODE = "phone_code";

    @Override
    public Authentication convert(HttpServletRequest request) {
        MultiValueMap<String, String> parameters = getFormParameters(request);

        // grant_type (REQUIRED)
        String grantType = parameters.getFirst(OAuth2ParameterNames.GRANT_TYPE);
        if (!MyAuthorizationGrantType.PHONE_CODE.getValue().equals(grantType)) {
            return null;
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        // phoneNum (REQUIRED)
        List<String> phoneNums = parameters.getOrDefault(PHONE_NUM, Collections.emptyList());
        String phoneNum = null;
        if (!CollectionUtils.isEmpty(phoneNums) && phoneNums.size() == 1) {
            String phoneNum_temp = phoneNums.get(0);
            // 这里可以校验电话号码格式，这里就不做校验了
            if (!StringUtils.hasText(phoneNum_temp)) {
                throwError(OAuth2ErrorCodes.INVALID_REQUEST, PHONE_NUM, null);
            }else{
                phoneNum = phoneNum_temp;
            }
        }

        // phoneNum (REQUIRED)
        List<String> phoneCodes = parameters.getOrDefault(PHONE_CODE, Collections.emptyList());
        String phoneCode = null;
        if (!CollectionUtils.isEmpty(phoneCodes) && phoneCodes.size() == 1) {
            String phoneCode_temp = phoneCodes.get(0);
            // 这里可以校验电话号码格式，这里就不做校验了
            if (!StringUtils.hasText(phoneCode_temp)) {
                throwError(OAuth2ErrorCodes.INVALID_REQUEST, PHONE_NUM, null);
            }else{
                phoneCode = phoneCode_temp;
            }
        }

        // scope (OPTIONAL)
        String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
        if (StringUtils.hasText(scope) && parameters.get(OAuth2ParameterNames.SCOPE).size() != 1) {
            throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.SCOPE,null);
        }

        Set<String> requestedScopes = null;
        if (StringUtils.hasText(scope)) {
            requestedScopes = new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        }

        return new PhoneCodeAuthenticationToken(MyAuthorizationGrantType.PHONE_CODE, clientPrincipal, phoneCode, phoneNum, requestedScopes);
    }


    private MultiValueMap<String, String> getFormParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameterMap.forEach((key, values) -> {
            String queryString = StringUtils.hasText(request.getQueryString()) ? request.getQueryString() : "";
            // If not query parameter then it's a form parameter
            if (!queryString.contains(key) && values.length > 0) {
                for (String value : values) {
                    parameters.add(key, value);
                }
            }
        });
        return parameters;
    }

    private void throwError(String errorCode, String parameterName, String errorUri) {
        OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
        throw new OAuth2AuthenticationException(error);
    }
}
