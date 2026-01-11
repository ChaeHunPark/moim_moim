package com.example.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;


@SpringBootApplication
class BackendApplication {
	public static void main(String[] args) {
		// 1. .env 파일 로드
		Dotenv dotenv = Dotenv.configure().load();

		// 2. 읽어온 값을 시스템 프로퍼티로 등록 (Spring이 ${}로 인식할 수 있게 함)
		System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
		SpringApplication.run(BackendApplication.class, args);
	}

}
