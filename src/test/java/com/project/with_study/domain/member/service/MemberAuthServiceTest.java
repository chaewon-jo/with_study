package com.project.with_study.domain.member.service;

import com.project.with_study.domain.member.dto.AddressDto;
import com.project.with_study.domain.member.dto.request.MemberJoinRequest;
import com.project.with_study.domain.member.dto.request.MemberLoginRequest;
import com.project.with_study.domain.member.entity.Address;
import com.project.with_study.domain.member.entity.Authority;
import com.project.with_study.domain.member.entity.Member;
import com.project.with_study.domain.member.exception.errorcode.MemberErrorCode;
import com.project.with_study.domain.member.repository.MemberRepository;
import com.project.with_study.global.config.security.JwtTokenProvider;
import com.project.with_study.global.exception.BusinessException;
import com.project.with_study.global.exception.errorcode.CommonErrorCode;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MemberAuthServiceTest {

    @InjectMocks
    MemberAuthService memberAuthService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setup() {
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("회원가입 성공 - 정상적인 요청인 경우 멤버가 정상 저장된다")
    void join_success() {
        MemberJoinRequest requestDto = createBaseMemberJoinDto().build();
        when(memberRepository.existsByEmail(requestDto.email())).thenReturn(false);
        when(memberRepository.existsByPhoneNumber(requestDto.phoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(requestDto.password())).thenReturn("encode_password");

        Member mockMember = createBaseMember().build();
        when(memberRepository.save(any(Member.class))).thenReturn(mockMember);

        memberAuthService.join(requestDto);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class); // ArgumentCaptor: Member 객체 캡쳐용
        verify(memberRepository, times(1)).save(captor.capture()); //save() 호출 시 사용된 mockMember 캡쳐
        verify(passwordEncoder, times(1)).encode(requestDto.password());
        assertThat(captor.getValue().getEmail()).isEqualTo(requestDto.email());
        assertThat(captor.getValue().getPassword()).isEqualTo("encode_password");
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void join_failed_by_duplicate_email() {
        MemberJoinRequest requestDto = createBaseMemberJoinDto().build();
        when(memberRepository.existsByEmail(requestDto.email())).thenReturn(true);

        assertThatThrownBy(() -> memberAuthService.join(requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(MemberErrorCode.DUPLICATE_EMAIL.getMessage());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 전화번호")
    void join_failed_by_duplicate_phonenumber() {
        MemberJoinRequest requestDto = createBaseMemberJoinDto().build();
        when(memberRepository.existsByPhoneNumber(requestDto.phoneNumber())).thenReturn(true);

        assertThatThrownBy(() -> memberAuthService.join(requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(MemberErrorCode.DUPLICATE_PHONENUMBER.getMessage());
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호와 비밀번호 확인의 불일치")
    void join_failed_by_password_confirm_mismatch() {
        MemberJoinRequest requestDto = createBaseMemberJoinDto()
                .checkPassword("mismatch1234!")
                .build();
        when(memberRepository.existsByEmail(requestDto.email())).thenReturn(false);
        when(memberRepository.existsByPhoneNumber(requestDto.phoneNumber())).thenReturn(false);

        assertThatThrownBy(() -> memberAuthService.join(requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage(MemberErrorCode.PASSWORD_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("로그인 성공 - Redis에 RT 저장 및 AT, RT 쿠키 생성")
    void login_success() {
        MemberLoginRequest requestDto = createBaseMemberLoginDto().build();
        Member member = createBaseMember().build();

        when(memberRepository.findByEmail(requestDto.email())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(requestDto.password(), member.getPassword())).thenReturn(true);

        when(jwtTokenProvider.createToken(1L, "MEMBER")).thenReturn("mock_access_token");
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("mock_refresh_token");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        memberAuthService.login(requestDto, response);

        verify(valueOperations, times(1))
                .set(
                        eq(JwtTokenProvider.REFRESH_TOKEN_INITIAL + ": 1"),
                        eq("mock_refresh_token"),
                        eq(JwtTokenProvider.REFRESH_TOKEN_EXPIRED_TIME),
                        any()
                );

        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).hasSize(2);
        String accessCookieHeader = response.getHeaders(HttpHeaders.SET_COOKIE).get(0);
        assertThat(accessCookieHeader).contains("AT=mock_access_token", "HttpOnly", "Secure", "Lax");
        String refreshCookieHeader = response.getHeaders(HttpHeaders.SET_COOKIE).get(1);
        assertThat(refreshCookieHeader).contains("RT=mock_refresh_token", "HttpOnly", "Secure", "Lax");

    }

    @DisplayName("로그인 실패 - 이메일에 맞는 회원 존재하지 않음")
    @Test
    void login_failed_by_not_found_email() {
        MemberLoginRequest requestDto = createBaseMemberLoginDto().build();

        when(memberRepository.findByEmail(requestDto.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberAuthService.login(requestDto, response))
                .isInstanceOf(BusinessException.class)
                .hasMessage(MemberErrorCode.LOGIN_MISMATCH.getMessage());
    }

    @DisplayName("로그인 실패 - 비밀번호 오류")
    @Test
    void login_failed_by_password_mismatch() {
        MemberLoginRequest requestDto = createBaseMemberLoginDto().build();
        Member member = createBaseMember().build();

        when(memberRepository.findByEmail(requestDto.email())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(requestDto.password(), member.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> memberAuthService.login(requestDto, response))
                .isInstanceOf(BusinessException.class)
                .hasMessage(MemberErrorCode.LOGIN_MISMATCH.getMessage());
    }

    @DisplayName("로그아웃 성공 - Redis에서 RT 삭제 + AT 블랙리스트에 등록")
    @Test
    void logout_success() {
        request.setCookies(new Cookie(JwtTokenProvider.ACCESS_TOKEN_INITIAL, "mock_access_token"));
        when(jwtTokenProvider.resolveAccessToken(request)).thenReturn("mock_resolve_access_token");
        when(jwtTokenProvider.getMemberPKFromExpiredToken("mock_resolve_access_token")).thenReturn("10");

        when(jwtTokenProvider.validateToken("mock_resolve_access_token")).thenReturn(true); // 살아있는 토큰
        when(jwtTokenProvider.getExpiration("mock_resolve_access_token")).thenReturn(50000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        memberAuthService.logout(request, response);

        verify(redisTemplate, times(1)).delete(JwtTokenProvider.REFRESH_TOKEN_INITIAL + ": 10"); // RT 삭제
        verify(valueOperations, times(1)).set(eq("mock_resolve_access_token"), eq("logout"), eq(50000L), any()); // AT 블랙리스트 등록

        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE).get(0)).contains("Max-Age=0"); //제거된 AT 수명 0으로 확인
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE).get(1)).contains("Max-Age=0"); //제거된 RT 수명 0으로 확인
    }

    @Test
    @DisplayName("로그아웃 성공(수명이 끝난 토큰) - Redis에서 RT 제거, 블랙리스트에 AT를 추가로 등록하지 않는다.")
    void logout_Success_With_ExpiredToken() {
        request.setCookies(new Cookie(JwtTokenProvider.ACCESS_TOKEN_INITIAL, "expired_token"));
        when(jwtTokenProvider.resolveAccessToken(request)).thenReturn("expired_token");
        when(jwtTokenProvider.getMemberPKFromExpiredToken("expired_token")).thenReturn("10");

        when(jwtTokenProvider.validateToken("expired_token")).thenReturn(false); // 토큰 수명 만료

        memberAuthService.logout(request, response);

        verify(redisTemplate, times(1)).delete(JwtTokenProvider.REFRESH_TOKEN_INITIAL + ": 10");
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any()); // 블랙리스트 유지
    }

    @Test
    @DisplayName("로그아웃 실패 - 유효한 토큰이 존재하지 않는다.")
    void logout_failed_by_invalid_token() {
        request.setCookies(new Cookie(JwtTokenProvider.ACCESS_TOKEN_INITIAL, "expired_token"));
        when(jwtTokenProvider.resolveAccessToken(request)).thenReturn(null);

        assertThatThrownBy(() -> memberAuthService.logout(request, response))
                .isInstanceOf(BusinessException.class)
                .hasMessage(CommonErrorCode.INVALID_TOKEN.getMessage());
    }

    @Test
    @DisplayName("토큰 재발급 성공 - 유효한 RT가 들어오고 Redis의 저장된 RT와 일치하면 새 토큰 발급")
    void reissue_Success() {
        request.setCookies(new Cookie(JwtTokenProvider.REFRESH_TOKEN_INITIAL, "refresh_token"));
        when(jwtTokenProvider.resolveRefreshToken(request)).thenReturn("resolved_refresh_token");
        when(jwtTokenProvider.validateToken("resolved_refresh_token")).thenReturn(true);
        when(jwtTokenProvider.getMemberPK("resolved_refresh_token")).thenReturn("10");

        String redisKey = JwtTokenProvider.REFRESH_TOKEN_INITIAL + ": 10";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn("resolved_refresh_token");

        Member member = Member.builder().id(10L).authority(Authority.MEMBER).build();
        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));

        when(jwtTokenProvider.createToken(10L, "MEMBER")).thenReturn("new_access_token");
        when(jwtTokenProvider.createRefreshToken(10L)).thenReturn("new_refresh_token");

        memberAuthService.reissue(request, response);

        verify(valueOperations, times(1)).set(eq(redisKey), eq("new_refresh_token"), anyLong(), any());
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE).get(0)).contains("AT=new_access_token");
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE).get(1)).contains("RT=new_refresh_token");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효한 RT지만 Redis에 저장된 RT와 다르다.")
    void reissue_fail_tokenMismatch() {
        // (멤버 ID: 10) 요청 쿠키에 저장된 RT 정보
        request.setCookies(new Cookie(JwtTokenProvider.REFRESH_TOKEN_INITIAL, "refresh_token"));
        when(jwtTokenProvider.resolveRefreshToken(request)).thenReturn("refresh_token");
        when(jwtTokenProvider.validateToken("refresh_token")).thenReturn(true);
        when(jwtTokenProvider.getMemberPK("refresh_token")).thenReturn("10");

        // (멤버 ID: 10) Redis에 저장된 실제 RT 정보 - 요청 쿠키에 있는 값과 다르다.
        String redisKey = JwtTokenProvider.REFRESH_TOKEN_INITIAL + ": 10";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn("another_refresh_token");

        assertThatThrownBy(() -> memberAuthService.reissue(request, response))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CommonErrorCode.TOKEN_MISMATCH.getMessage());
        verifyNoInteractions(redisTemplate, memberRepository);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - RT가 없으면 INVALID_TOKEN")
    void reissue_fail_noRefreshTokenResolve() {
        when(jwtTokenProvider.resolveRefreshToken(request)).thenReturn(null);

        assertThatThrownBy(() -> memberAuthService.reissue(request, response))
                .isInstanceOf(BusinessException.class)
                .hasMessage(CommonErrorCode.INVALID_TOKEN.getMessage());

        verifyNoInteractions(redisTemplate, memberRepository);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - RT가 유효하지 않으면 INVALID_TOKEN")
    void reissue_fail_invalidRefreshToken() {
        request.setCookies(new Cookie(JwtTokenProvider.REFRESH_TOKEN_INITIAL, "invalid_rt"));
        when(jwtTokenProvider.resolveRefreshToken(request)).thenReturn("invalid_rt");
        when(jwtTokenProvider.validateToken("invalid_rt")).thenReturn(false);

        assertThatThrownBy(() -> memberAuthService.reissue(request, response))
                .isInstanceOf(BusinessException.class)
                .hasMessage(CommonErrorCode.INVALID_TOKEN.getMessage());
        verifyNoInteractions(redisTemplate, memberRepository);
    }

    private static MemberJoinRequest.MemberJoinRequestBuilder createBaseMemberJoinDto() {
        return MemberJoinRequest.builder()
                .name("홍길동")
                .password("abcd1234!")
                .checkPassword("abcd1234!")
                .addressDto(new AddressDto(
                        "서울시 xxx",
                        "xx아파트 xx동 xx호",
                        "123456"
                ))
                .phoneNumber("010-1234-5678")
                .nickname("아무개")
                .email("abcd1234@gmail.com");
    }


    private static MemberLoginRequest.MemberLoginRequestBuilder createBaseMemberLoginDto() {
        return MemberLoginRequest.builder()
                .email("abcd1234@gmail.com")
                .password("test1234!");
    }

    private Member.MemberBuilder createBaseMember() {
        return Member.builder()
                .id(1L)
                .name("홍길동")
                .password("abcd1234!")
                .address(Address.builder()
                        .base("서울시 ...")
                        .detail("xx아파트 xx동 xx호")
                        .postalCode("123456")
                        .build())
                .phoneNumber("010-1234-5678")
                .nickname("아무개")
                .email("abcd1234@gmail.com");
    }
}