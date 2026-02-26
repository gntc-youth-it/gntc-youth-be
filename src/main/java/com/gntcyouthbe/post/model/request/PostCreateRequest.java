package com.gntcyouthbe.post.model.request;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.post.domain.PostSubCategory;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @NotNull(message = "카테고리는 필수입니다")
    private PostSubCategory subCategory;

    private String content;

    private List<String> hashtags;

    private List<ChurchId> churches;

    private List<Long> imageIds;
}
