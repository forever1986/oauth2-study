package com.demo.lesson19.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DemoController {

    @GetMapping("/demo")
    public Mono<String> demo() {
        return Mono.just("demo");
    }
}
