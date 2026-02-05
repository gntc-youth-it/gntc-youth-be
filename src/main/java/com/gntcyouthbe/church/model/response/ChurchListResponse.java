package com.gntcyouthbe.church.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.church.domain.ChurchId;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChurchListResponse {
    private final List<ChurchResponse> churches;

    public ChurchListResponse() {
        this.churches = Arrays.stream(ChurchId.values())
                .map(ChurchResponse::new)
                .toList();
    }
}
