package com.gntcyouthbe.zoo.model.response;

import java.util.List;

public record ZooPhotoListResponse(
        List<ZooPhotoResponse> photos
) {
}
