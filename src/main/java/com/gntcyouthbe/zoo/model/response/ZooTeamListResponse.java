package com.gntcyouthbe.zoo.model.response;

import java.util.List;

public record ZooTeamListResponse(
        List<ZooTeamSummaryResponse> teams
) {
}
