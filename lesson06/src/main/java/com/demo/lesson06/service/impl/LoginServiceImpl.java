package com.demo.lesson06.service.impl;

import com.demo.lesson06.dto.LoginDTO;
import com.demo.lesson06.entity.LoginUserDetails;
import com.demo.lesson06.result.Result;
import com.demo.lesson06.service.LoginService;
import com.demo.lesson06.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisTemplate redisTemplate;

    private static String PRE_KEY = "user:";

    @Override
    public Result<String> login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            if(authentication!=null && authentication.isAuthenticated()){
                SecurityContextHolder.getContext().setAuthentication(authentication);
                LoginUserDetails user = (LoginUserDetails)authentication.getPrincipal();
                String subject = PRE_KEY + user.getTUser().getId();
                String token = JwtUtil.createToken(subject, 1000*60*5L);
                redisTemplate.opsForValue().set(subject, user, 1000*60*5L, TimeUnit.MILLISECONDS);
                return Result.success(token);
            }
        }catch (AuthenticationException e){
            return Result.failed(e.getLocalizedMessage());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return Result.failed("认证失败");
    }

    @Override
    public Result<String> logout() {
        if(SecurityContextHolder.getContext().getAuthentication()!=null){
            LoginUserDetails loginUserDetails = (LoginUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(loginUserDetails!=null){
                String key = PRE_KEY + loginUserDetails.getTUser().getId();
                redisTemplate.delete(key);
            }else {
                return Result.failed("登出失败，用户不存在");
            }
        }
        return Result.success("登出成功");
    }
}
