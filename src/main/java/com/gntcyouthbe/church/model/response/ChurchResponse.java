package com.gntcyouthbe.church.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.church.domain.ChurchId;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChurchResponse {
    private final String code;
    private final String name;

    public ChurchResponse(ChurchId churchId) {
        this.code = churchId.name();
        this.name = churchId.getDisplayName();
    }
}
