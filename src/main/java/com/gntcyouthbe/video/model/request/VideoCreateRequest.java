package com.gntcyouthbe.video.model.request;

import com.gntcyouthbe.post.domain.PostSubCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class VideoCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String link;

    @NotNull
    private PostSubCategory subCategory;
}
