package com.gntcyouthbe.thanks.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThanksEntryCreateRequestTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("감사제목의 앞뒤 공백을 지운다")
    void strip() {
        assertThat(new ThanksEntryCreateRequest("  점심 사 준 동기에게 감사\n", 31L).content())
                .isEqualTo("점심 사 준 동기에게 감사");
    }

    @Test
    @DisplayName("앞뒤 공백을 뺀 길이로 80자까지 받는다")
    void content_maxLength() {
        assertThat(messagesOf(new ThanksEntryCreateRequest("  " + "감".repeat(80) + "  ", 31L))).isEmpty();
        assertThat(messagesOf(new ThanksEntryCreateRequest("감".repeat(81), 31L)))
                .containsExactly("감사제목은 80자까지 쓸 수 있어요.");
    }

    @Test
    @DisplayName("감사제목이 없거나 공백뿐이면 받지 않는다")
    void content_blank() {
        assertThat(messagesOf(new ThanksEntryCreateRequest(null, 31L))).containsExactly("감사제목을 입력해 주세요.");
        assertThat(messagesOf(new ThanksEntryCreateRequest("   ", 31L))).containsExactly("감사제목을 입력해 주세요.");
    }

    @Test
    @DisplayName("파일이 없어도 1001로 거르지 않는다. 서비스가 3001로 응답한다")
    void fileId_null() {
        assertThat(messagesOf(new ThanksEntryCreateRequest("점심 감사", null))).isEmpty();
    }

    private List<String> messagesOf(ThanksEntryCreateRequest request) {
        return VALIDATOR.validate(request).stream()
                .map(ConstraintViolation::getMessage)
                .toList();
    }
}
