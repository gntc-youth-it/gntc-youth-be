package com.gntcyouthbe.user.model.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class UserNameUpdateRequest {

    private final String newName;

    @JsonCreator
    public UserNameUpdateRequest(@JsonProperty("new_name") String newName) {
        this.newName = newName;
    }
}
