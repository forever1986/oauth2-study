package com.demo.lesson02.client.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2LoginController {

    // http://localhost:8080/demo
    @GetMapping("/demo")
    public String demo() {
        return "demo";
    }
}
