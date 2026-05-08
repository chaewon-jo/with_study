package com.project.with_study.domain.member.entity;

import lombok.*;
import org.hibernate.annotations.Comment;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {
    @Comment("우편번호")
    private String postalCode;

    @Comment("기본 주소")
    private String base;

    @Comment("상세 주소")
    private String detail;
}
