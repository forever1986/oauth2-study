package com.demo.lesson20.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class Oauth2Lesson20GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(Oauth2Lesson20GatewayApplication.class, args);
    }

}
