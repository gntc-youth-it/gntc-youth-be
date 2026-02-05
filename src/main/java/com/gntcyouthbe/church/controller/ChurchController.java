package com.gntcyouthbe.church.controller;

import com.gntcyouthbe.church.model.response.ChurchListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/churches")
public class ChurchController {

    @GetMapping
    public ResponseEntity<ChurchListResponse> getChurches() {
        return ResponseEntity.ok(new ChurchListResponse());
    }
}
