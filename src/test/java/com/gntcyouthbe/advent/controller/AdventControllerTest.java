package com.gntcyouthbe.advent.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gntcyouthbe.advent.model.response.AdventVerseListResponse;
import com.gntcyouthbe.advent.service.AdventService;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.model.ExceptionCode;
import com.gntcyouthbe.common.security.configuration.SecurityConfig;
import com.gntcyouthbe.common.security.service.JwtService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AdventController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        excludeAutoConfiguration = {OAuth2ClientAutoConfiguration.class, OAuth2ClientWebSecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class AdventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdventService adventService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /advent - 어드벤트 구절을 반환한다")
    void getAdventVerses() throws Exception {
        given(adventService.getAdventVerses("테스트", "Brisbane", 2024))
                .willReturn(new AdventVerseListResponse("테스트", "Brisbane", 2024, List.of()));

        mockMvc.perform(get("/advent")
                        .param("name", "테스트")
                        .param("temple", "Brisbane")
                        .param("batch", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트"));
    }

    @Test
    @DisplayName("GET /advent - 필수 파라미터 누락 시 400을 반환한다")
    void getAdventVersesMissingParams() throws Exception {
        mockMvc.perform(get("/advent"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /advent - 사용자 미존재 시 404를 반환한다")
    void getAdventVersesNotFound() throws Exception {
        given(adventService.getAdventVerses("없는사람", "Brisbane", 2024))
                .willThrow(new EntityNotFoundException(ExceptionCode.ADVENT_PERSON_NOT_FOUND));

        mockMvc.perform(get("/advent")
                        .param("name", "없는사람")
                        .param("temple", "Brisbane")
                        .param("batch", "2024"))
                .andExpect(status().isNotFound());
    }
}
