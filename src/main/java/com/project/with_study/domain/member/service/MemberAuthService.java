package com.project.with_study.domain.member.service;

import com.project.with_study.domain.member.dto.request.MemberJoinRequest;
import com.project.with_study.domain.member.dto.request.MemberLoginRequest;
import com.project.with_study.domain.member.entity.Member;
import com.project.with_study.domain.member.exception.errorcode.MemberErrorCode;
import com.project.with_study.domain.member.repository.MemberRepository;
import com.project.with_study.global.config.security.JwtTokenProvider;
import com.project.with_study.global.exception.BusinessException;
import com.project.with_study.global.exception.errorcode.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.project.with_study.global.config.security.JwtTokenProvider.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void login(MemberLoginRequest request, HttpServletResponse response) {
        String email = request.email();
        String password = request.password();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.LOGIN_MISMATCH));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BusinessException(MemberErrorCode.LOGIN_MISMATCH);
        }

        String accessToken = jwtTokenProvider.createToken(member.getId(), member.getAuthority().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_INITIAL + ": " + member.getId(),
                refreshToken,
                REFRESH_TOKEN_EXPIRED_TIME,
                TimeUnit.MILLISECONDS
        );

        log.info("[LOGIN]: 유저 ID: {}가 로그인하였습니다.", member.getId());

        setCookie(ACCESS_TOKEN_INITIAL, accessToken, ACCESS_TOKEN_EXPIRED_TIME, response);
        setCookie(REFRESH_TOKEN_INITIAL, refreshToken, REFRESH_TOKEN_EXPIRED_TIME, response);
    }

    @Transactional
    public void join(MemberJoinRequest request) {
        validateJoinMember(request);

        Member member = request.toMember(passwordEncoder);
        memberRepository.save(member);
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.resolveAccessToken(request);

        if (accessToken == null) {
            throw new BusinessException(CommonErrorCode.INVALID_TOKEN);
        }

        String memberId = jwtTokenProvider.getMemberPKFromExpiredToken(accessToken);
        redisTemplate.delete(REFRESH_TOKEN_INITIAL + ": " + memberId);

        if (jwtTokenProvider.validateToken(accessToken)) {
            Long expiration = jwtTokenProvider.getExpiration(accessToken);
            redisTemplate.opsForValue().set(
                    accessToken,
                    "logout",
                    expiration,
                    TimeUnit.MILLISECONDS
            );

            log.info("[LOGOUT] 유저 ID {} 가 로그아웃하였습니다.", memberId);

        } else {
            log.info("[LOGOUT] 유저 ID {}의 RT를 삭제했습니다.", memberId);
        }

        deleteCookie(ACCESS_TOKEN_INITIAL, response);
        deleteCookie(REFRESH_TOKEN_INITIAL, response);

        SecurityContextHolder.clearContext();
    }

    @Transactional
    public void reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(CommonErrorCode.INVALID_TOKEN);
        }

        String memberId = jwtTokenProvider.getMemberPK(refreshToken);

        String redisKey = REFRESH_TOKEN_INITIAL + ": " + memberId;
        String savedRefreshToken = redisTemplate.opsForValue().get(redisKey);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(CommonErrorCode.TOKEN_MISMATCH);
        }

        Member member = memberRepository.findById(Long.parseLong(memberId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다."));

        String newAccessToken = jwtTokenProvider.createToken(member.getId(), member.getAuthority().name());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        redisTemplate.opsForValue().set(
                redisKey,
                newRefreshToken,
                REFRESH_TOKEN_EXPIRED_TIME,
                TimeUnit.MILLISECONDS
        );

        log.info("[REISSUE]: 유저 ID: {} 의 토큰이 재발급되었습니다.", memberId);

        setCookie(ACCESS_TOKEN_INITIAL, newAccessToken, ACCESS_TOKEN_EXPIRED_TIME, response);
        setCookie(REFRESH_TOKEN_INITIAL, newRefreshToken, REFRESH_TOKEN_EXPIRED_TIME, response);
    }

    private void validateJoinMember(MemberJoinRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_EMAIL);
        }

        if (memberRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_PHONENUMBER);
        }

        request.checkPassword();
    }

    private void setCookie(String tokenInitial, String token, long expireTime, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from(tokenInitial, token)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofMillis(expireTime))
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteCookie(String tokenInitial, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from(tokenInitial, "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

