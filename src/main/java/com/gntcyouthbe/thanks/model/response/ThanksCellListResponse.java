package com.gntcyouthbe.thanks.model.response;

import java.util.List;

// 구역은 화면에 보여 줄 순서대로 담는다. myCellId는 요청한 사람이 고른 구역이고, 아직 안 골랐으면 null이다.
public record ThanksCellListResponse(
        List<ThanksCellResponse> cells,
        Long myCellId
) {
}
