package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String nickname;
    private Integer age;

    @JsonProperty("region_id")
    private Long regionId;

    private String bio;
}