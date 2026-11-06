package com.gntcyouthbe.thanks.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThanksMyCellUpdateRequestTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("구역 id가 있으면 받는다")
    void cellId() {
        assertThat(messagesOf(new ThanksMyCellUpdateRequest(2L))).isEmpty();
    }

    @Test
    @DisplayName("구역 id가 없으면 받지 않는다")
    void cellId_null() {
        assertThat(messagesOf(new ThanksMyCellUpdateRequest(null))).containsExactly("구역을 골라 주세요.");
    }

    private List<String> messagesOf(ThanksMyCellUpdateRequest request) {
        return VALIDATOR.validate(request).stream()
                .map(ConstraintViolation::getMessage)
                .toList();
    }
}
