package com.gntcyouthbe.user.model.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserNameUpdateRequest {

    @NotBlank(message = "이름은 필수입니다")
    private final String newName;

    @JsonCreator
    public UserNameUpdateRequest(@JsonProperty("new_name") String newName) {
        this.newName = newName;
    }
}
