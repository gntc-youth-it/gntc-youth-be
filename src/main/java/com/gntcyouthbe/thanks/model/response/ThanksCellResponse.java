package com.gntcyouthbe.thanks.model.response;

import com.gntcyouthbe.thanks.domain.ThanksCell;

public record ThanksCellResponse(
        Long id,
        String name
) {

    public static ThanksCellResponse from(ThanksCell cell) {
        return new ThanksCellResponse(cell.getId(), cell.getName());
    }
}
