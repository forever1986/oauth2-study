package com.demo.lesson08.controller;

import com.demo.lesson08.entity.UserInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserInfoController {

    // http://localhost:8081/userInfo
    @GetMapping("/userInfo")
    public UserInfo getUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(1l);
        userInfo.setUsernName("测试用户");
        userInfo.setUserEmail("13888888888@163.com");
        userInfo.setUserPhone("13888888888");
        return userInfo;
    }

}
