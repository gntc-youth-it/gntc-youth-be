package com.gntcyouthbe.zoo.model.request;

import com.gntcyouthbe.zoo.domain.ZooCourse;
import jakarta.validation.constraints.NotNull;

public record ZooTeamCourseUpdateRequest(
        @NotNull(message = "코스를 골라 주세요.")
        ZooCourse course
) {
}
