package com.gntcyouthbe.video.model.request;

import com.gntcyouthbe.post.domain.PostSubCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;

@Getter
public class VideoCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    @URL
    private String link;

    @NotNull
    private PostSubCategory subCategory;
}
