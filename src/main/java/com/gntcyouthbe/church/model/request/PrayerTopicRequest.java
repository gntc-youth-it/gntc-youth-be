package com.gntcyouthbe.church.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PrayerTopicRequest {

    @NotBlank
    private String content;

    @NotNull
    private Integer sortOrder;
}
