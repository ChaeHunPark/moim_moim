package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {


    // 💡 생성자 추가: 이 Bean이 등록되었는지 확인하는 로그
    public SecurityConfig() {
        System.out.println("########## SecurityConfig Bean Loaded Successfully ##########");
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("########## SecurityFilterChain Configuration Applied ##########");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests((auth) ->
                        auth.requestMatchers("/**","/api/**").permitAll()
                                .anyRequest().permitAll())
                                // 💡 2. Actuator 엔드포인트 접근을 인증 없이 허용
                                //    (Health, Info 엔드포인트는 보통 허용합니다.)


                .formLogin(AbstractHttpConfigurer::disable)   // 🔥 로그인 폼 자동 생성 막기

                .logout(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable) // <-- 추가

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));




        return http.build();
    }

}
