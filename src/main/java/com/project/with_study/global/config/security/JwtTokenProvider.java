package com.project.with_study.global.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final RedisTemplate<String, String> redisTemplate;

    //무슨 역할?
    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    public static final long ACCESS_TOKEN_EXPIRED_TIME = 1000 * 60 * 60 * 2; //2시간
    public static final String REFRESH_TOKEN_INITIAL = "RT";
    public static final long REFRESH_TOKEN_EXPIRED_TIME = 1000 * 60 * 60 * 24 * 7; //7일

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(this.secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(final Long id, final String role) {
        final Date now = new Date();

        String token = Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .issuer("with_study.com")
                .subject(String.valueOf(id))  // 유저 PK를 Subject에 바인딩
                .claim(AUTHORITIES_KEY, role) // 유저 권한을 Claims에 바인딩
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRED_TIME))
                .signWith(this.secretKey, Jwts.SIG.HS256)
                .compact();

        log.info("[JWT TOKEN] 토큰 생성 완료");

        return token;
    }

    public String resolveToken(final HttpServletRequest request) {
        final String bearerToken = request.getHeader(AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * JJWT 0.12.x 버전 규격에 맞춘 토큰 서명 및 만료 유효성 검증
     */
    public boolean validateToken(final String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public Authentication getAuthentication(final String accessToken) {
        final Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new IllegalArgumentException("권한 정보가 없는 토큰입니다.");
        }

        // 1. 클레임에서 권한(Role) 리스트 파싱
        final Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // 2. Subject에서 유저 고유 ID 추출
        final String memberId = claims.getSubject();

        // 3. MemberDetailService 조회 없이 메모리 상에서 즉시 UserDetails 구현체 조립
        final UserDetails userDetails = new User(memberId, "", authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, accessToken, authorities);
    }

    /**
     * Redis를 활용한 무상태(Stateless) 강제 로그아웃 검증 (프로젝트 핵심 조건 충족)
     */
    public boolean isTokenLoggedOut(final String token) {
        final String logoutValue = redisTemplate.opsForValue().get(token);
        if (StringUtils.hasText(logoutValue)) {
            log.warn("로그아웃 처리된 폐기 토큰으로의 우회 접근이 감지되었습니다.");
            return true;
        }
        return false;
    }

    /**
     * 유저 고유 식별자(PK) 추출
     */
    public String getMemberPK(final String token) {
        return parseClaims(token).getSubject();
    }

    public Long getExpiration(final String token) {
        final Date expiration = parseClaims(token).getExpiration();
        final long now = System.currentTimeMillis();

        return Math.max(0, expiration.getTime() - now);
    }

    /**
     * JJWT 0.12.x 사양의 Claims 직렬화 캡슐화 (구형 getBody 대치)
     */
    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload(); // 0.12.x 권장 표준 사양 적용
    }
}
