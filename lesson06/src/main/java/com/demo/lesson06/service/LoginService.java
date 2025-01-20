package com.demo.lesson06.service;


import com.demo.lesson06.dto.LoginDTO;
import com.demo.lesson06.result.Result;

public interface LoginService {

    Result<String> login(LoginDTO loginDTO);

    Result<String> logout();
}
