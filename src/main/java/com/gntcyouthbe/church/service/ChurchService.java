package com.gntcyouthbe.church.service;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.model.response.ChurchListResponse;
import com.gntcyouthbe.church.model.response.ChurchResponse;
import java.util.Arrays;
import org.springframework.stereotype.Service;

@Service
public class ChurchService {

    private final ChurchListResponse churchListResponse = new ChurchListResponse(
            Arrays.stream(ChurchId.values())
                    .map(ChurchResponse::from)
                    .toList()
    );

    public ChurchListResponse getChurches() {
        return churchListResponse;
    }
}
