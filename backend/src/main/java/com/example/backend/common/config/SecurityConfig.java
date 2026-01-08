package com.example.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("########## SecurityFilterChain Configuration Applied ##########");

        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests((auth) ->
                        auth.requestMatchers("/api/auth/**").permitAll()
                                .anyRequest().permitAll())


                .formLogin(AbstractHttpConfigurer::disable)   // 🔥 로그인 폼 자동 생성 막기

                .logout(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable) // <-- 추가

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));




        return http.build();
    }

}
