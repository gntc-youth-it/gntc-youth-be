package com.gntcyouthbe.church.model.response;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChurchListResponse {
    private final List<ChurchResponse> churches;
}
