package com.project.with_study.domain.member.entity;

import com.project.with_study.domain.participant.entity.Participant;
import com.project.with_study.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE member SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("회원 이름")
    @NotBlank
    @Size(min = 2, max = 8)
    @Column(length = 8, nullable = false)
    private String name;

    @Comment("닉네임")
    @NotBlank
    @Size(max = 16)
    @Column(length = 16, nullable = false)
    private String nickname;

    @Comment("비밀번호")
    @NotBlank
    @Column(length = 60, nullable = false)
    private String password;

    @Comment("이메일")
    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Comment("전화번호")
    @NotBlank
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Comment("주소")
    @Embedded
    @NotNull
    private Address address;

    @Comment("회원 권한")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    @Builder.Default
    private Authority authority = Authority.MEMBER;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Participant> participants = new ArrayList<>();

}
