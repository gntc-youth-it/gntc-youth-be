package com.gntcyouthbe.thanks.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThanksEntryUpdateRequestTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("감사제목의 앞뒤 공백을 지운다")
    void strip() {
        assertThat(new ThanksEntryUpdateRequest("  점심 사 준 동기와 선배에게 감사\n").content())
                .isEqualTo("점심 사 준 동기와 선배에게 감사");
    }

    @Test
    @DisplayName("앞뒤 공백을 뺀 길이로 80자까지 받는다")
    void content_maxLength() {
        assertThat(messagesOf(new ThanksEntryUpdateRequest("  " + "고".repeat(80) + "  "))).isEmpty();
        assertThat(messagesOf(new ThanksEntryUpdateRequest("고".repeat(81))))
                .containsExactly("감사제목은 80자까지 쓸 수 있어요.");
    }

    @Test
    @DisplayName("감사제목이 없거나, 비었거나, 공백뿐이면 받지 않는다")
    void content_blank() {
        assertThat(messagesOf(new ThanksEntryUpdateRequest(null))).containsExactly("감사제목을 입력해 주세요.");
        assertThat(messagesOf(new ThanksEntryUpdateRequest(""))).containsExactly("감사제목을 입력해 주세요.");
        assertThat(messagesOf(new ThanksEntryUpdateRequest("   "))).containsExactly("감사제목을 입력해 주세요.");
    }

    private List<String> messagesOf(ThanksEntryUpdateRequest request) {
        return VALIDATOR.validate(request).stream()
                .map(ConstraintViolation::getMessage)
                .toList();
    }
}
