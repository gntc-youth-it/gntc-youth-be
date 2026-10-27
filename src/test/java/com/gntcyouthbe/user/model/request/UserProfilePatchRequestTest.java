package com.gntcyouthbe.user.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserProfilePatchRequestTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("이름의 앞뒤 공백을 지운다")
    void strip() {
        assertThat(new UserProfilePatchRequest("  김하늘\n", null).name()).isEqualTo("김하늘");
    }

    @Test
    @DisplayName("앞뒤 공백을 뺀 길이로 20자까지 받는다")
    void name_maxLength() {
        assertThat(messagesOf(new UserProfilePatchRequest("  " + "가".repeat(20) + "  ", null))).isEmpty();
        assertThat(messagesOf(new UserProfilePatchRequest("가".repeat(21), null)))
                .containsExactly("이름은 1자에서 20자까지 쓸 수 있어요.");
    }

    @Test
    @DisplayName("보낸 이름이 비었거나 공백뿐이면 받지 않는다")
    void name_blank() {
        assertThat(messagesOf(new UserProfilePatchRequest("", null))).containsExactly("이름은 1자에서 20자까지 쓸 수 있어요.");
        assertThat(messagesOf(new UserProfilePatchRequest("   ", 10L))).containsExactly("이름은 1자에서 20자까지 쓸 수 있어요.");
    }

    @Test
    @DisplayName("이름이나 사진 중 하나만 보내도 된다")
    void eitherField() {
        assertThat(messagesOf(new UserProfilePatchRequest("김하늘", null))).isEmpty();
        assertThat(messagesOf(new UserProfilePatchRequest(null, 10L))).isEmpty();
    }

    @Test
    @DisplayName("이름도 사진도 없으면 받지 않는다")
    void nothingToChange() {
        assertThat(messagesOf(new UserProfilePatchRequest(null, null))).containsExactly("바꿀 이름이나 사진을 보내 주세요.");
    }

    private List<String> messagesOf(UserProfilePatchRequest request) {
        return VALIDATOR.validate(request).stream()
                .map(ConstraintViolation::getMessage)
                .toList();
    }
}
