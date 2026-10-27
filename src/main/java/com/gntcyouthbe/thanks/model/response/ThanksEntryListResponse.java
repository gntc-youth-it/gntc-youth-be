package com.gntcyouthbe.thanks.model.response;

import java.time.LocalDate;
import java.util.List;

// 순서는 FE가 사람별로 묶어 정한다. 하루 많아야 60개 정도라 페이지를 나누지 않는다.
public record ThanksEntryListResponse(
        LocalDate date,
        List<ThanksEntryResponse> entries
) {
}
