package com.project.with_study.domain.member.dto.response;

import com.project.with_study.domain.member.dto.AddressDto;

import java.time.LocalDateTime;

public record MemberInfoResponse(
        Long id,
        String email,
        String nickname,
        String phoneNumber,
        AddressDto address,
        LocalDateTime createdAt
) {
}
