package com.demo.lesson20.gateway.controller;

import com.demo.lesson20.gateway.security.ResourceServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class RedisController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/init")
    public void init() {
        Map<String, List<String>> urlPermRolesRules = new ConcurrentHashMap<>();
        List<String> roles = new ArrayList<>();
        // /api-a/*的所有接口都需要api-a的角色才能访问
        roles.add("api-a");
        urlPermRolesRules.put("/api-a/*", roles);
        redisTemplate.opsForHash().putAll(ResourceServerManager.AUTHORITIES_URL_ROLE,urlPermRolesRules);
    }
}
