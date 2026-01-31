package com.gntcyouthbe.advent.controller;

import com.gntcyouthbe.advent.model.response.AdventVerseListResponse;
import com.gntcyouthbe.advent.service.AdventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/advent")
@RequiredArgsConstructor
public class AdventController {

    private final AdventService adventService;

    @GetMapping
    public ResponseEntity<AdventVerseListResponse> getAdventVerses(
            @RequestParam String name,
            @RequestParam String temple,
            @RequestParam Integer batch
    ) {
        return ResponseEntity.ok(adventService.getAdventVerses(name, temple, batch));
    }
}
