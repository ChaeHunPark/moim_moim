package com.example.backend.controller;

import com.example.backend.dto.MemberResponse;
import com.example.backend.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(Authentication authentication) {
        // 인증 객체에서 ID 추출 (Long 타입으로 변환)
        Long memberId = Long.valueOf(authentication.getName());

        MemberResponse myInfo = memberService.getMyInfo(memberId);
        return ResponseEntity.ok(myInfo);
    }
}
