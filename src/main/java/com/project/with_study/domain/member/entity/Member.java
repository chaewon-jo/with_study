package com.project.with_study.domain.member.entity;

import com.project.with_study.domain.roommember.entity.RoomMember;
import com.project.with_study.global.entity.BaseEntity;
import jakarta.persistence.*;
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
    @Column(length = 8, nullable = false)
    private String name;

    @Comment("닉네임")
    @Column(length = 16, nullable = false)
    private String nickname;

    @Comment("비밀번호")
    @Column(length = 24, nullable = false)
    private String password;

    @Comment("이메일")
    @Column(nullable = false, unique = true)
    private String email;

    @Comment("전화번호")
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Comment("주소")
    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<RoomMember> roomMembers = new ArrayList<>();

}
