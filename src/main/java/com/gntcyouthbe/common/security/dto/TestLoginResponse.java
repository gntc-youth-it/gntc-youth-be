package com.gntcyouthbe.common.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestLoginResponse {
    private String accessToken;
    private Long userId;
    private String email;
    private String name;
    private String role;
}
