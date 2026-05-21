package com.project.with_study.domain.member.controller;

import com.project.with_study.domain.member.dto.request.MemberJoinRequest;
import com.project.with_study.domain.member.dto.request.MemberLoginRequest;
import com.project.with_study.domain.member.service.MemberAuthService;
import com.project.with_study.global.response.SingleApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberAuthController {
    private final MemberAuthService memberAuthService;

    @PostMapping("/signup")
    public ResponseEntity<SingleApiResponse<String>> join(@Valid @RequestBody MemberJoinRequest requestDto) {
        memberAuthService.join(requestDto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .replacePath("/api/member/{id}")
                .buildAndExpand(requestDto.email())
                .toUri();

        return ResponseEntity
                .created(uri)
                .body(SingleApiResponse.of("회원가입이 완료되었습니다.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<SingleApiResponse<String>> login(@Valid @RequestBody MemberLoginRequest requestDto, HttpServletResponse response) {
        memberAuthService.login(requestDto, response);

        return ResponseEntity.ok(
                SingleApiResponse.of("로그인이 완료되었습니다.", null)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<SingleApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        memberAuthService.logout(request, response);

        return ResponseEntity.ok(
                SingleApiResponse.of("로그아웃이 완료되었습니다.", null)
        );
    }

    @PostMapping("/reissue")
    public ResponseEntity<SingleApiResponse<String>> reissue(HttpServletRequest request, HttpServletResponse response) {
        memberAuthService.reissue(request, response);

        return ResponseEntity.ok(
                SingleApiResponse.of("재발급이 완료되었습니다.", null)
        );
    }

}
