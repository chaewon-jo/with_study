package com.project.with_study.domain.posting.repository;

import com.project.with_study.domain.member.entity.Address;
import com.project.with_study.domain.member.entity.Member;
import com.project.with_study.domain.member.repository.MemberRepository;
import com.project.with_study.domain.posting.entity.Post;
import com.project.with_study.global.config.JpaAuditConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

@Import(JpaAuditConfig.class)
@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private EntityManager em;
    private JPAQueryFactory jpaQueryFactory;
    @Autowired
    private PostRepository postRepository;
    private PostSearchRepository searchRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setup() {
        this.jpaQueryFactory = new JPAQueryFactory(em);
        this.searchRepository = new PostSearchRepository(jpaQueryFactory);
    }

    @DisplayName("BaseEntity Auditing 적용 확인")
    @Test
    @WithMockUser("test_user")
    void auditing_test() {
        Post saved = postRepository.save(
                createBasePost().build()
        );

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getModifiedAt()).isNotNull();
        assertThat(saved.getCreatedBy()).isEqualTo("test_user");
        assertThat(saved.getLastModifiedBy()).isEqualTo("test_user");
        assertThat(saved.isDeleted()).isFalse();
    }

    private Post.PostBuilder createBasePost() {
        Member member = createBaseMember().build();
        memberRepository.save(member);

        return Post.builder()
                .member(member)
                .title("Test Title")
                .content("""
                        ```
                        Test Content
                        ```
                        """);
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