package com.gntcyouthbe.church.controller;

import com.gntcyouthbe.church.model.response.ChurchListResponse;
import com.gntcyouthbe.church.service.ChurchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/churches")
@RequiredArgsConstructor
public class ChurchController {

    private final ChurchService churchService;

    @GetMapping
    public ResponseEntity<ChurchListResponse> getChurches() {
        return ResponseEntity.ok(new ChurchListResponse(churchService.getChurches()));
    }
}
