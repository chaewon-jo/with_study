package com.project.with_study.domain.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.with_study.domain.member.dto.AddressDto;
import com.project.with_study.domain.member.dto.request.MemberJoinRequest;
import com.project.with_study.domain.member.dto.request.MemberLoginRequest;
import com.project.with_study.domain.member.entity.Member;
import com.project.with_study.domain.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static com.project.with_study.global.config.security.JwtTokenProvider.ACCESS_TOKEN_INITIAL;
import static com.project.with_study.global.config.security.JwtTokenProvider.REFRESH_TOKEN_INITIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("회원가입 시나리오")
    void join_scenario() throws Exception {
        MemberJoinRequest joinRequest = createBaseJoinDto("abcd1234@gmail.com", "010-1234-5678");

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isCreated());

        assertThat(memberRepository.existsByEmail("abcd1234@gmail.com")).isTrue();
        assertThat(memberRepository.findByEmail("abcd1234@gmail.com").get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("로그인 시나리오 - 쿠키(AT, RT) 값과 Redis에 저장된 RT 확인")
    void login_scenario() throws Exception {
        String email = "abcd1234@gmail.com";
        executeJoin(email, "010-1234-5678");
        Member joinMember = memberRepository.findByEmail(email).get();
        MemberLoginRequest loginRequest = createBaseLoginDto(email);


        MvcResult result = mockMvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessCookie = result.getResponse().getCookie(ACCESS_TOKEN_INITIAL);
        Cookie refreshCookie = result.getResponse().getCookie(REFRESH_TOKEN_INITIAL);
        assertThat(accessCookie).isNotNull();
        assertThat(refreshCookie).isNotNull();

        String redisKey = REFRESH_TOKEN_INITIAL + ": " + joinMember.getId();
        assertThat(redisTemplate.opsForValue().get(redisKey)).isEqualTo(refreshCookie.getValue());
    }

    @Test
    @DisplayName("로그아웃 시나리오 - 정상적인 쿠키값(RT)를 가져오면, Redis에서 RT 파기 및 블랙리스트에 등록")
    void logout_scenario() throws Exception {
        String email = "abcd1234@gmail.com";
        Cookie accessCookie = executeLoginThrowAccessCookie(email, "010-1234-5678");

        mockMvc.perform(post("/api/member/logout")
                        .cookie(accessCookie)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk());

        Member member = memberRepository.findByEmail(email).get();
        String redisKey = REFRESH_TOKEN_INITIAL + ": " + member.getId();
        assertThat(redisTemplate.opsForValue().get(redisKey)).isNull();
        assertThat(redisTemplate.opsForValue().get(accessCookie.getValue())).isEqualTo("logout");
    }

    @Test
    @DisplayName("토큰 재발급 시나리오 - 정상적인 RT를 가져오면 새 AT, RT 발급 및 Redis에 값 교체")
    void reissue_scenario() throws Exception {
        String email = "abcd1234@gmail.com";
        Cookie refreshCookie = executeLoginThrowRefreshCookie(email, "010-1234-5678");

        MvcResult result = mockMvc.perform(post("/api/member/reissue")
                        .cookie(refreshCookie)
                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Cookie newAccessCookie = result.getResponse().getCookie(ACCESS_TOKEN_INITIAL);
        Cookie newRefreshCookie = result.getResponse().getCookie(REFRESH_TOKEN_INITIAL);
        assertThat(newAccessCookie).isNotNull();
        assertThat(newRefreshCookie).isNotNull();

        assertThat(newRefreshCookie.getValue()).isNotEqualTo(refreshCookie.getValue());

        Member member = memberRepository.findByEmail(email).get();
        String redisKey = REFRESH_TOKEN_INITIAL + ": " + member.getId();
        String newRefreshToken = redisTemplate.opsForValue().get(redisKey);
        assertThat(newRefreshToken).isNotEqualTo(refreshCookie.getValue());
        assertThat(newRefreshToken).isEqualTo(newRefreshCookie.getValue());
    }

    private void executeJoin(String email, String phoneNumber) throws Exception {
        MemberJoinRequest joinRequest = createBaseJoinDto(email, phoneNumber);

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isCreated());
    }

    private Cookie executeLoginThrowAccessCookie(String email, String phoneNumber) throws Exception {
        MvcResult result = executeLogin(email, phoneNumber);

        return result.getResponse().getCookie(ACCESS_TOKEN_INITIAL);
    }

    private Cookie executeLoginThrowRefreshCookie(String email, String phoneNumber) throws Exception {
        MvcResult result = executeLogin(email, phoneNumber);

        return result.getResponse().getCookie(REFRESH_TOKEN_INITIAL);
    }

    private MvcResult executeLogin(String email, String phoneNumber) throws Exception {
        executeJoin(email, phoneNumber);
        MemberLoginRequest loginRequest = createBaseLoginDto(email);

        return mockMvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private static MemberJoinRequest createBaseJoinDto(String email, String phoneNumber) {
        return MemberJoinRequest.builder()
                .name("홍길동")
                .nickname("아무개")
                .email(email)
                .phoneNumber(phoneNumber)
                .password("test1234!")
                .checkPassword("test1234!")
                .addressDto(new AddressDto(
                        "서울시 xxx",
                        "xx아파트 xx동 xx호",
                        "123456"
                ))
                .build();
    }

    private MemberLoginRequest createBaseLoginDto(String email) {
        return MemberLoginRequest.builder()
                .email(email)
                .password("test1234!")
                .build();
    }

}
