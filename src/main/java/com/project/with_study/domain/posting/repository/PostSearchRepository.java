package com.project.with_study.domain.posting.repository;

import com.project.with_study.domain.posting.PostStatus;
import com.project.with_study.domain.posting.dto.SearchCriteria;
import com.project.with_study.domain.posting.dto.request.PostSearchCondition;
import com.project.with_study.domain.posting.entity.Post;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

import static com.project.with_study.domain.member.entity.QMember.member;
import static com.project.with_study.domain.posting.entity.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostSearchRepository implements SearchRepository<Post, PostSearchCondition> {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Post> search(PostSearchCondition condition, Pageable pageable) {
        List<Post> result = jpaQueryFactory.selectFrom(post)
                .leftJoin(post.member, member).fetchJoin()
                .where(
                        checkPostStatus(condition.status()),
                        checkKeyword(condition.criteria(), condition.keyword())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(toOrderSpecifier())
                .fetch();

        Long total = jpaQueryFactory
                .select(post.count())
                .from(post)
                .leftJoin(post.member, member)
                .where(
                        checkPostStatus(condition.status()),
                        checkKeyword(condition.criteria(), condition.keyword())
                )
                .fetchOne();

        long totalCount = (total != null) ? total : 0L;

        return new PageImpl<>(result, pageable, totalCount);
    }

    private BooleanExpression checkPostStatus(PostStatus status) {
        if (status == null) {
            return null;
        }

        return post.status.eq(status);
    }

    private BooleanExpression checkKeyword(SearchCriteria criteria, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return switch (criteria) {
            case CONTENT -> post.content.contains(keyword);
            case NICKNAME -> post.member.nickname.contains(keyword);
            case TITLE -> post.title.contains(keyword);
            case TITLE_AND_CONTENT -> post.title.contains(keyword).or(post.content.contains(keyword));
        };
    }

    /**
     * 기본적으로 최신순 조회
     *
     * @return OrderSpecifier - QeuryDsl의 정렬 표현식
     */
    private OrderSpecifier<?>[] toOrderSpecifier() {
            return new OrderSpecifier[]{post.createdAt.desc()};
    }
}
