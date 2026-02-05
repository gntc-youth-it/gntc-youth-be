package com.gntcyouthbe.church.service;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.model.response.ChurchResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ChurchService {

    private final List<ChurchResponse> churchList = Arrays.stream(ChurchId.values())
            .map(ChurchResponse::from)
            .toList();

    public List<ChurchResponse> getChurches() {
        return churchList;
    }
}
