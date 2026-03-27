package com.example.backend.controller;

import com.example.backend.dto.MemberResponse;
import com.example.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(Authentication authentication) {
        Long memberId = Long.valueOf(authentication.getName());
        MemberResponse myInfo = memberService.getMyInfo(memberId);
        return ResponseEntity.ok(myInfo);
    }
}
