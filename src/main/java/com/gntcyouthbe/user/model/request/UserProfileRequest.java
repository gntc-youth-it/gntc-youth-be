package com.gntcyouthbe.user.model.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gntcyouthbe.user.domain.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class UserProfileRequest {

    @NotNull(message = "기수는 필수입니다")
    @Positive(message = "기수는 양수여야 합니다")
    private final Integer generation;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private final String phoneNumber;

    @NotNull(message = "성별은 필수입니다")
    private final Gender gender;

    @JsonCreator
    public UserProfileRequest(
            @JsonProperty("generation") Integer generation,
            @JsonProperty("phone_number") String phoneNumber,
            @JsonProperty("gender") Gender gender) {
        this.generation = generation;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }
}
