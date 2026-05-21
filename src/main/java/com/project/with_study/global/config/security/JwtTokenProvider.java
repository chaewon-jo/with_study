package com.project.with_study.global.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
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

    /**
     * 접속 토큰 생성 로직
     *
     * @param id   사용자 id
     * @param role 사용자의 권한
     */
    public String createToken(final Long id, final String role) {
        final Date now = new Date();

        String token = Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .issuer("with_study.com")
                .subject(String.valueOf(id))
                .claim(AUTHORITIES_KEY, role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRED_TIME))
                .signWith(this.secretKey, Jwts.SIG.HS256)
                .compact();

        log.info("[JWT TOKEN] 토큰 생성 완료");

        return token;
    }

    /**
     * 리프레시 토큰 생성 로직
     *
     * @param id 사용자 id
     * Redis에 저장된 사용자의 Access Token과 대조하는 토큰
     */
    public String createRefreshToken(final Long id) {
        final Date now = new Date();

        String token = Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .issuer("with_study.com")
                .subject(String.valueOf(id))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRED_TIME))
                .signWith(this.secretKey, Jwts.SIG.HS256)
                .compact();

        log.info("[JWT REFRESH TOKEN] 토큰 생성 완료");

        return token;
    }

    public String resolveToken(final HttpServletRequest request) {
        final String bearerToken = request.getHeader(AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

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

        final Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        final String memberId = claims.getSubject();

        final UserDetails userDetails = new User(memberId, "", authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, accessToken, authorities);
    }

    public boolean isTokenLoggedOut(final String token) {
        try {
            final String logoutValue = redisTemplate.opsForValue().get(token);

            if (StringUtils.hasText(logoutValue)) {
                log.warn("로그아웃 처리된 폐기 토큰으로의 우회 접근이 감지되었습니다.");
                return true;
            }

            return false;
        } catch (DataAccessException e) {
            log.error("[Redis] 토큰 조회 실패", e);
            return true;
        }
    }

    public String getMemberPK(final String token) {
        return parseClaims(token).getSubject();
    }

    public Long getExpiration(final String token) {
        final Date expiration = parseClaims(token).getExpiration();
        final long now = System.currentTimeMillis();

        return Math.max(0, expiration.getTime() - now);
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
