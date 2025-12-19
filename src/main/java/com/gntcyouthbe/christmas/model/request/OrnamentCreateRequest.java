package com.gntcyouthbe.christmas.model.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gntcyouthbe.christmas.domain.OrnamentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class OrnamentCreateRequest {

    @NotBlank
    @Size(max = 50)
    private final String writerName;

    @NotNull
    private final OrnamentType type;

    @NotBlank
    @Size(max = 200)
    private final String message;

    @NotNull
    private final Double x;

    @NotNull
    private final Double y;

    @JsonCreator
    public OrnamentCreateRequest(
            @JsonProperty("writer_name") String writerName,
            @JsonProperty("type") OrnamentType type,
            @JsonProperty("message") String message,
            @JsonProperty("x") Double x,
            @JsonProperty("y") Double y
    ) {
        this.writerName = writerName;
        this.type = type;
        this.message = message;
        this.x = x;
        this.y = y;
    }
}
