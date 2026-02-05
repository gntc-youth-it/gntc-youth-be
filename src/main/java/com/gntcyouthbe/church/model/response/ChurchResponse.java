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

    private ChurchResponse(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ChurchResponse from(ChurchId churchId) {
        return new ChurchResponse(churchId.name(), churchId.getDisplayName());
    }
}
