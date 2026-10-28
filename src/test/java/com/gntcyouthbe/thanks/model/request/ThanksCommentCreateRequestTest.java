package com.gntcyouthbe.thanks.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThanksCommentCreateRequestTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("댓글의 앞뒤 공백을 지운다")
    void strip() {
        assertThat(new ThanksCommentCreateRequest("  기도할게요 🙏\n").content()).isEqualTo("기도할게요 🙏");
    }

    @Test
    @DisplayName("앞뒤 공백을 뺀 길이로 100자까지 받는다")
    void content_maxLength() {
        assertThat(messagesOf(new ThanksCommentCreateRequest("  " + "댓".repeat(100) + "  "))).isEmpty();
        assertThat(messagesOf(new ThanksCommentCreateRequest("댓".repeat(101))))
                .containsExactly("댓글은 100자까지 쓸 수 있어요.");
    }

    @Test
    @DisplayName("댓글이 없거나 공백뿐이면 받지 않는다")
    void content_blank() {
        assertThat(messagesOf(new ThanksCommentCreateRequest(null))).containsExactly("댓글을 입력해 주세요.");
        assertThat(messagesOf(new ThanksCommentCreateRequest("   "))).containsExactly("댓글을 입력해 주세요.");
    }

    private List<String> messagesOf(ThanksCommentCreateRequest request) {
        return VALIDATOR.validate(request).stream()
                .map(ConstraintViolation::getMessage)
                .toList();
    }
}
