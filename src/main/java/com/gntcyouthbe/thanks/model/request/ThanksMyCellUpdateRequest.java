package com.gntcyouthbe.thanks.model.request;

import jakarta.validation.constraints.NotNull;

public record ThanksMyCellUpdateRequest(
        @NotNull(message = "구역을 골라 주세요.")
        Long cellId
) {
}
