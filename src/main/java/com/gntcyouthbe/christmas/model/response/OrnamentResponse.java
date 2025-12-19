package com.gntcyouthbe.christmas.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.christmas.domain.Ornament;
import com.gntcyouthbe.christmas.domain.OrnamentType;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrnamentResponse {

    private final Long id;

    private final String writerName;

    private final OrnamentType type;

    private final String message;

    private final Double x;

    private final Double y;

    public OrnamentResponse(Ornament ornament) {
        this.id = ornament.getId();
        this.writerName = ornament.getWriterName();
        this.type = ornament.getType();
        this.message = ornament.getMessage();
        this.x = ornament.getPosition().getX();
        this.y = ornament.getPosition().getY();
    }
}
