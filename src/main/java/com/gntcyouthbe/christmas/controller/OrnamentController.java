package com.gntcyouthbe.christmas.controller;

import com.gntcyouthbe.christmas.model.request.OrnamentCreateRequest;
import com.gntcyouthbe.christmas.model.response.OrnamentResponse;
import com.gntcyouthbe.christmas.service.OrnamentService;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ornaments")
@RequiredArgsConstructor
public class OrnamentController {

    private final OrnamentService ornamentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrnamentResponse> createOrnament(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody OrnamentCreateRequest request
    ) {
        final OrnamentResponse response = ornamentService.createOrnament(userPrincipal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrnamentResponse>> getAllOrnaments() {
        final List<OrnamentResponse> response = ornamentService.getAllOrnaments();
        return ResponseEntity.ok(response);
    }
}
