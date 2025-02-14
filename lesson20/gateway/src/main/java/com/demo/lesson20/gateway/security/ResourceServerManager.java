package com.demo.lesson20.gateway.security;

import com.alibaba.nacos.shaded.com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ResourceServerManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private static final String AUTHORIZATION_KEY ="Authorization";

    private static final String AUTHORITIES_SCOPE_PREFIX = "SCOPE_";

    public static final String AUTHORITIES_URL_ROLE = "AUTHORITIES_URL_ROLE";

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> mono, AuthorizationContext authorizationContext) {
        ServerHttpRequest request = authorizationContext.getExchange().getRequest();
        // 预检请求放行
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return Mono.just(new AuthorizationDecision(true));
        }
        // 访问登录和授权的请求，放行
        PathMatcher pathMatcher = new AntPathMatcher();
        String path = request.getURI().getPath();
        if (pathMatcher.match("/auth/**", path)|| pathMatcher.match("/init", path)) {
            return Mono.just(new AuthorizationDecision(true));
        }
        // 判断token（必须在Authorization中有）
        String token = "";
        boolean tokenCheckFlag = false;
        if(Objects.nonNull(request.getHeaders().getFirst(AUTHORIZATION_KEY)) && !Strings.isNullOrEmpty(request.getHeaders().getFirst(AUTHORIZATION_KEY))){
            token = request.getHeaders().getFirst(AUTHORIZATION_KEY);
            tokenCheckFlag = true;
        }
        if (!tokenCheckFlag) {
            return Mono.just(new AuthorizationDecision(false));
        }
        log.info("判断权限");

        // 缓存中获取url与角色的对应关系
        Map<String, List<String>> urlPermRolesRules = redisTemplate.opsForHash().entries(AUTHORITIES_URL_ROLE);

        // 获取当前资源 所需要的角色
        List<String> authorizedRoles = new ArrayList<>(); // 拥有访问权限的角色
        for (Map.Entry<String, List<String>> permRoles : urlPermRolesRules.entrySet()) {
            String perm = permRoles.getKey();
            if (pathMatcher.match(perm, path)) {
                List<String> values = permRoles.getValue();
                authorizedRoles.addAll(values);
            }
        }

        // 判断JWT中携带的scope，对应在缓存中有/api-a/*权限的角色
        Mono<AuthorizationDecision> authorizationDecisionMono = mono
                .filter(Authentication::isAuthenticated)
                .flatMapIterable(Authentication::getAuthorities)
                .map(GrantedAuthority::getAuthority)
                .any(authority -> {
                    String roleCode = authority.substring(AUTHORITIES_SCOPE_PREFIX.length()); // 用户的角色
                    boolean hasAuthorized = !CollectionUtils.isEmpty(authorizedRoles) && authorizedRoles.contains(roleCode);
                    return hasAuthorized;
                })
                .map(AuthorizationDecision::new)
                .defaultIfEmpty(new AuthorizationDecision(false));

        return authorizationDecisionMono;
    }
}
