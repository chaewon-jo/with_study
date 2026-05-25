package com.project.with_study.domain.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.with_study.domain.member.dto.AddressDto;
import com.project.with_study.domain.member.dto.request.MemberJoinRequest;
import com.project.with_study.domain.member.dto.request.MemberLoginRequest;
import com.project.with_study.domain.member.service.MemberAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = MemberAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberAuthService memberAuthService;

    @DisplayName("회원가입을 진행한다.")
    @Test
    void join_memeber_suceess() throws Exception {
        MemberJoinRequest request = createBaseMemberJoinDto().build();

        //어떤 requestDto가 memberAuthService.join()에 들어가든, 아무 반응도 해서는 안 된다.
        doNothing().when(memberAuthService).join(any(MemberJoinRequest.class));

        mockMvc.perform(post("/api/member/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @DisplayName("회원가입 실패 - DTO 제약값 위반 시 400 에러 발생")
    @ParameterizedTest
    @MethodSource("invalidJoinRequest")
    void join_failed_by_boundary_validation(String description, MemberJoinRequest requestDto) throws Exception {
        mockMvc.perform(post("/api/member/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("로그인을 진행한다.")
    @Test
    void login_success() throws Exception{
        MemberLoginRequest request = createBaseMemberLoginDto().build();

        doNothing().when(memberAuthService).login(any(MemberLoginRequest.class), any());

        mockMvc.perform(post("/api/member/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인이 완료되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @DisplayName("로그인 실패 - DTO 제약값 위반 시 400 에러 발생")
    @ParameterizedTest
    @MethodSource("invalidLoginRequest")
    void login_falied_by_boundary_validation(String description, MemberLoginRequest requestDto) throws Exception {
        mockMvc.perform(post("/api/member/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("로그아웃을 진행한다.")
    @Test
    void logout_success() throws Exception{
        doNothing().when(memberAuthService).logout(any(), any());

        mockMvc.perform(post("/api/member/logout"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @DisplayName("토큰 재발급을 진행한다.")
    @Test
    void reissue_success() throws Exception{
        doNothing().when(memberAuthService).reissue(any(), any());

        mockMvc.perform(post("/api/member/reissue"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("재발급이 완료되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    private static Stream<Arguments> invalidJoinRequest() {
        return Stream.of(
                Arguments.of("name 공백", createBaseMemberJoinDto().name(" ").build()),
                Arguments.of("name 2자 미만", createBaseMemberJoinDto().name("A").build()),
                Arguments.of("name 8자 초과", createBaseMemberJoinDto().name("ABCDEFGHI").build()),
                Arguments.of("nickname 공백", createBaseMemberJoinDto().nickname(" ").build()),
                Arguments.of("nickname 2자 미만", createBaseMemberJoinDto().nickname("A").build()),
                Arguments.of("nickname 16자 초과", createBaseMemberJoinDto().nickname("123456789ABCDEFGH").build()),
                Arguments.of("password 공백", createBaseMemberJoinDto().password(" ").build()),
                Arguments.of("password 패턴 불일치", createBaseMemberJoinDto().password("test1234567").build()),
                Arguments.of("checkPassword 공백", createBaseMemberJoinDto().checkPassword(" ").build()),
                Arguments.of("email 공백", createBaseMemberJoinDto().email(" ").build()),
                Arguments.of("email 형식 오류", createBaseMemberJoinDto().email("test1234").build()),
                Arguments.of("phoneNumber 공백", createBaseMemberJoinDto().phoneNumber(" ").build()),
                Arguments.of("phoneNumber 형식 오류", createBaseMemberJoinDto().phoneNumber("01012345678").build()),
                Arguments.of("addressDto - base 공백", createBaseMemberJoinDto()
                        .addressDto(
                                new AddressDto(
                                        " ",
                                        "xx아파트 xx동 xx호",
                                        "123456"))
                        .build()),
                Arguments.of("addressDto - detail 공백", createBaseMemberJoinDto()
                        .addressDto(
                                new AddressDto(
                                        "서울시 xxx",
                                        " ",
                                        "123456"))
                        .build()),
                Arguments.of("addressDto - postalCode 공백", createBaseMemberJoinDto()
                        .addressDto(
                                new AddressDto(
                                        "서울시 xxx",
                                        "xx아파트 xx동 xx호",
                                        " "))
                        .build())
        );
    }

    private static Stream<Arguments> invalidLoginRequest(){
        return Stream.of(
                Arguments.of("email 공백", createBaseMemberLoginDto().email(" ").build()),
                Arguments.of("email 형식 오류", createBaseMemberLoginDto().email("abcd1234").build()),
                Arguments.of("password 공백", createBaseMemberLoginDto().password(" ").build()),
                Arguments.of("password 형식 오류", createBaseMemberLoginDto().password("test1234").build())
        );
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
}