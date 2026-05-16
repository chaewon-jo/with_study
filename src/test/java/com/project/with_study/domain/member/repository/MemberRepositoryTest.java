package com.project.with_study.domain.member.repository;

import com.project.with_study.domain.member.entity.Address;
import com.project.with_study.domain.member.entity.Member;
import com.project.with_study.global.config.JpaAuditConfig;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(JpaAuditConfig.class)
@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("BaseEntity Auditing 적용 확인")
    @Test
    void auditing_test() {
        Member saved = memberRepository.save(
                createBaseMember().build()
        );

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getModifiedAt()).isNotNull();
        assertThat(saved.isDeleted()).isFalse();
    }

    @DisplayName("회원 저장 - 성공")
    @Test
    void save() {
        Member saved = memberRepository.save(
                createBaseMember().build()
        );

        assertThat(saved.getId()).isNotNull();
    }

    @DisplayName("회원 저장 - 실패: 이메일 중복")
    @Test
    void duplicate_email_fail() {
        memberRepository.save(
                createBaseMember().build()
        );

        Member duplicateMember = createBaseMember()
                .phoneNumber("010-1111-1111")
                .build();

        assertThatThrownBy(() ->
                memberRepository.save(duplicateMember))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * Bean Validation 예외 발생
     */
    @DisplayName("회원 저장 - 실패: 이메일 형식 오류")
    @ParameterizedTest
    @ValueSource(strings = {"test", "test@", " ", "\t"})
    @NullAndEmptySource
    void email_form_fail(String email) {
        Member member = createBaseMember()
                .email(email)
                .build();

        assertThatThrownBy(() ->
                memberRepository.save(member))
                .isInstanceOf(ConstraintViolationException.class);
    }

    /**
     * DB 테이블 조건에 위배 - DataIntegrityViolationException 발생
     */
    @DisplayName("회원 저장 - 실패: 전화번호 중복")
    @Test
    void duplicate_phoneNumber_fail() {
        memberRepository.save(
                createBaseMember().build()
        );
        Member duplicateMember = createBaseMember()
                .email("test@gmail.com")
                .build();

        assertThatThrownBy(() ->
                memberRepository.save(duplicateMember))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("회원 저장 - 성공: 닉네임 길이에 따른 성공 케이스들")
    @ParameterizedTest
    @ValueSource(strings = {"A", "ABCDEFGHIJKLMMOP"})
    void save_nickname_length_success(String nickname) {
        Member saved = memberRepository.save(
                createBaseMember()
                        .nickname(nickname)
                        .build()
        );

        assertThat(saved.getId()).isNotNull();
    }

    @DisplayName("회원 저장 - 실패: 닉네임 길이 초과로 인한 실패")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "\t", "ABCDEFGHIJKLMMOPR"})
    void save_nickname_length_fail(String nickname) {
        assertThatThrownBy(() ->
                memberRepository.save(
                        createBaseMember()
                                .nickname(nickname)
                                .build()
                ))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @DisplayName("id로 회원 찾기")
    @Test
    void findById() {
        Member saved = memberRepository.save(
                createBaseMember().build()
        );

        Optional<Member> foundMember = memberRepository.findById(saved.getId());

        assertThat(foundMember.get().getName()).isEqualTo("홍길동");
    }

    @DisplayName("회원 삭제: 소프트 딜리트 적용 확인")
    @Test
    void softDelete() {
        Member saved = memberRepository.save(
                createBaseMember().build()
        );

        memberRepository.delete(saved);
        memberRepository.flush(); // DB에 즉시 반영

        Optional<Member> found = memberRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    private Member.MemberBuilder createBaseMember() {
        return Member.builder()
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