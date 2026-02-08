package com.gntcyouthbe.file.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUrlResponse {

    private final Long fileId;
    private final String presignedUrl;
}
