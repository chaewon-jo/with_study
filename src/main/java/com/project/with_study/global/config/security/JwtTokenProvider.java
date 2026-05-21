package com.project.with_study.global.config.security;

import com.project.with_study.global.exception.BusinessException;
import com.project.with_study.global.exception.errorcode.CommonErrorCode;
import com.project.with_study.global.exception.errorcode.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
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
    public static final long ACCESS_TOKEN_EXPIRED_TIME = 1000 * 60 * 60 * 2; //2시간
    public static final long REFRESH_TOKEN_EXPIRED_TIME = 1000 * 60 * 60 * 24 * 7; //7일
    public static final String ACCESS_TOKEN_INITIAL = "AT";
    public static final String REFRESH_TOKEN_INITIAL = "RT";

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

        return Jwts.builder()
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
    }

    /**
     * 리프레시 토큰 생성 로직
     *
     * @param id 사용자 id
     * Redis에 저장된 사용자의 Access Token과 대조하는 토큰
     */
    public String createRefreshToken(final Long id) {
        final Date now = new Date();

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .issuer("with_study.com")
                .subject(String.valueOf(id))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRED_TIME))
                .signWith(this.secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String resolveAccessToken(final HttpServletRequest request) {
        return resolveCookie(request, ACCESS_TOKEN_INITIAL);
    }

    public String resolveRefreshToken(final HttpServletRequest request) {
        return resolveCookie(request, REFRESH_TOKEN_INITIAL);
    }

    public boolean validateToken(final String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT WARN] 만료된 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT WARN] 유효하지 않은 토큰입니다: {}", e.getMessage());
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
                        .toList();

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

    public String getMemberPKFromExpiredToken(final String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject(); //만료 토큰의 유저 ID 반환
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.INVALID_TOKEN);
        }
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

    private String resolveCookie(final HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
