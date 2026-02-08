package com.gntcyouthbe.file.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlRequest {

    @NotBlank
    private String filename;

    @NotBlank
    private String contentType;
}
