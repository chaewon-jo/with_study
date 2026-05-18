package com.project.with_study.global.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        // Request Header에서 토큰 추출
        final String token = jwtTokenProvider.resolveToken(request);

        // 토큰이 유효하고, Redis Blacklist에 등록되지 않은 경우에만 인증 처리
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

            // Redis를 활용한 로그아웃 여부 체킹 로직 (JwtTokenProvider 내부에서 RedisTemplate 등으로 검증)
            final boolean isLoggedOut = jwtTokenProvider.isTokenLoggedOut(token);

            if (!isLoggedOut) {
                final Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
