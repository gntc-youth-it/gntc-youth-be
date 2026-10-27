package com.gntcyouthbe.thanks.controller;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.thanks.model.request.ThanksEntryCreateRequest;
import com.gntcyouthbe.thanks.model.response.ThanksEntryListResponse;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse;
import com.gntcyouthbe.thanks.service.ThanksEntryService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/thanks/entries")
@RequiredArgsConstructor
public class ThanksEntryController {

    private final ThanksEntryService thanksEntryService;

    // 그 날짜에 모든 사람이 올린 감사를 한 번에 준다
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ThanksEntryListResponse> getEntries(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(thanksEntryService.getEntries(userPrincipal, date));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ThanksEntryResponse> createEntry(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ThanksEntryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(thanksEntryService.createEntry(userPrincipal, request));
    }

    // 본인이나 MASTER만 지울 수 있다. 아니면 서비스에서 403(6004)으로 거절한다
    @DeleteMapping("/{entryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteEntry(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long entryId) {
        thanksEntryService.deleteEntry(userPrincipal, entryId);
        return ResponseEntity.noContent().build();
    }
}
