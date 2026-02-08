package com.gntcyouthbe.file.controller;

import com.gntcyouthbe.file.model.request.PresignedUrlRequest;
import com.gntcyouthbe.file.model.response.PresignedUrlResponse;
import com.gntcyouthbe.file.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/presigned-url")
    @PreAuthorize("hasAnyAuthority('LEADER', 'MASTER')")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
            @Valid @RequestBody PresignedUrlRequest request) {
        return ResponseEntity.ok(fileService.generatePresignedUrl(request));
    }
}
