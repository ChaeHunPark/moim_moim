package com.example.backend.service;

import com.example.backend.common.security.JwtTokenProvider;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.entity.Member;
import com.example.backend.entity.Region;
import com.example.backend.enums.MemberStatus;
import com.example.backend.enums.Role;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.RegionRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository, RegionRepository regionRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.regionRepository = regionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(String email, String password) {
        // 1. 사용자 존재 여부 확인
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        // 2. 비밀번호 일치 확인 (암호화 방식 사용)
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 틀렸습니다.");
        }

        // 3. 토큰 생성 및 반환
        // name을 사용해서 ROLE_USER를 그대로 넘긴다.
        return jwtTokenProvider.createToken(member.getEmail(), member.getRole().name());
    }


    @Transactional
    public void register(RegisterRequest request) {
        // 1. 중복 체크
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2. 지역 조회
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));

        // 3. 명세서에 따른 회원 객체 생성 (기본값 설정)
        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .age(request.getAge())
                .introduction(request.getBio()) // 프론트의 bio 필드 매핑
                .region(region)
                .role(Role.ROLE_USER)            // 기본 권한: USER
                .status(MemberStatus.ACTIVE) // 기본 상태: ACTIVE
                .points(0)                  // 초기 포인트: 0
                .level(1)                   // 초기 레벨: 1
                .build();

        // 4. DB 저장 (createdAt, updatedAt은 자동 입력됨)
        memberRepository.save(member);
    }
}
