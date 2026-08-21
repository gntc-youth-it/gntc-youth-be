package com.gntcyouthbe.post.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostSubCategoryTest {

    @Test
    @DisplayName("하위 프로그램은 상위 카테고리의 대분류를 따른다")
    void childProgram_inheritsCategoryFromParent() {
        assertThat(PostSubCategory.RETREAT_2026_SUMMER_SPORTS.getParent())
                .isEqualTo(PostSubCategory.RETREAT_2026_SUMMER);
        assertThat(PostSubCategory.RETREAT_2026_SUMMER_SPORTS.getCategory())
                .isEqualTo(PostCategory.RETREAT);
    }

    @Test
    @DisplayName("withChildren()은 자신과 하위 프로그램을 모두 반환한다")
    void withChildren_returnsSelfAndChildren() {
        assertThat(PostSubCategory.RETREAT_2026_SUMMER.withChildren()).containsExactly(
                PostSubCategory.RETREAT_2026_SUMMER,
                PostSubCategory.RETREAT_2026_SUMMER_SPORTS,
                PostSubCategory.RETREAT_2026_SUMMER_WALK,
                PostSubCategory.RETREAT_2026_SUMMER_ETC);
    }

    @Test
    @DisplayName("하위 프로그램이 없는 카테고리의 withChildren()은 자신만 반환한다")
    void withChildren_withoutChildren_returnsSelfOnly() {
        assertThat(PostSubCategory.NONE.withChildren())
                .containsExactly(PostSubCategory.NONE);
        assertThat(PostSubCategory.RETREAT_2026_SUMMER_SPORTS.withChildren())
                .containsExactly(PostSubCategory.RETREAT_2026_SUMMER_SPORTS);
    }

    @Test
    @DisplayName("겨울 수련회는 하위 프로그램으로 새 힘을 노래하라를 가진다")
    void winterRetreat_hasSingProgram() {
        assertThat(PostSubCategory.RETREAT_2026_WINTER.withChildren()).containsExactly(
                PostSubCategory.RETREAT_2026_WINTER,
                PostSubCategory.RETREAT_2026_WINTER_SING);
        assertThat(PostSubCategory.RETREAT_2026_WINTER_SING.getDisplayName())
                .isEqualTo("새 힘을 노래하라");
        assertThat(PostSubCategory.RETREAT_2026_WINTER_SING.getCategory())
                .isEqualTo(PostCategory.RETREAT);
        assertThat(PostSubCategory.RETREAT_2026_WINTER_SING.isTopLevel()).isFalse();
    }

    @Test
    @DisplayName("하위 프로그램은 최상위 카테고리가 아니다")
    void isTopLevel_falseForChildPrograms() {
        assertThat(PostSubCategory.RETREAT_2026_SUMMER.isTopLevel()).isTrue();
        assertThat(PostSubCategory.RETREAT_2026_WINTER.isTopLevel()).isTrue();
        assertThat(PostSubCategory.NONE.isTopLevel()).isTrue();
        assertThat(PostSubCategory.RETREAT_2026_SUMMER_SPORTS.isTopLevel()).isFalse();
        assertThat(PostSubCategory.RETREAT_2026_SUMMER_WALK.isTopLevel()).isFalse();
        assertThat(PostSubCategory.RETREAT_2026_SUMMER_ETC.isTopLevel()).isFalse();
    }
}
