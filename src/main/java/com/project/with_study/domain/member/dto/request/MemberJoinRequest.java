package com.project.with_study.domain.member.dto.request;

import com.project.with_study.domain.member.dto.AddressDto;
import com.project.with_study.domain.member.entity.Member;
import com.project.with_study.domain.member.exception.MemberBusinessException;
import com.project.with_study.domain.member.exception.errorcode.MemberErrorCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
public record MemberJoinRequest(
        @NotBlank(message = "성함을 입력해주세요.")
        @Size(min = 2, message = "성함은 2자 이상이어야 합니다.")
        String name,
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 16, message = "닉네임은 2~16자 사이여야 합니다.")
        String nickname,
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다.")
        String password,
        @NotBlank(message = "비밀번호를 재입력해주세요.")
        String checkPassword,
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email,
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식(010-0000-0000)이어야 합니다.")
        String phoneNumber,
        AddressDto addressDto
) {

        public Member toMember(PasswordEncoder encoder){
                return Member.builder()
                        .name(name)
                        .nickname(nickname)
                        .email(email)
                        .password(encoder.encode(password))
                        .phoneNumber(phoneNumber)
                        .address(addressDto.toAddress())
                        .build();
        }

        public void confirmPassword() {
                if (!password.equals(checkPassword)) {
                   throw new MemberBusinessException(MemberErrorCode.PASSWORD_MISMATCH);
                }
        }

}
