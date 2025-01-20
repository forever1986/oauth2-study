package com.demo.lesson06.controller;

import com.demo.lesson06.dto.LoginDTO;
import com.demo.lesson06.result.Result;
import com.demo.lesson06.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 重新定义Spring Security的登录接口
 */
@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO loginDTO) {
        return loginService.login(loginDTO);
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        return loginService.logout();
    }

}
