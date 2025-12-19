package com.example.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {
    @GetMapping("/api/data")
    public String getData() {
        // 간단한 문자열 메시지 반환
        return "Backend Response: Hello from Spring Boot 8080! (CORS Global Setting Applied)";
    }
}
