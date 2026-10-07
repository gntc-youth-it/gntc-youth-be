package com.gntcyouthbe.zoo.model.request;

import jakarta.validation.constraints.NotNull;

public record ZooTeamLeaderTransferRequest(
        @NotNull(message = "조장을 넘길 조원을 골라 주세요.")
        Long userId
) {
}
