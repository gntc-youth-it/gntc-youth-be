package com.gntcyouthbe.church.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gntcyouthbe.church.model.response.ChurchListResponse;
import com.gntcyouthbe.church.model.response.ChurchResponse;
import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.service.ChurchService;
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

@WebMvcTest(value = ChurchController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        excludeAutoConfiguration = {OAuth2ClientAutoConfiguration.class, OAuth2ClientWebSecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class ChurchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChurchService churchService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /churches - 교회 목록을 반환한다")
    void getChurches() throws Exception {
        given(churchService.getChurches()).willReturn(
                new ChurchListResponse(List.of(
                        ChurchResponse.from(ChurchId.ANYANG)
                )));

        mockMvc.perform(get("/churches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.churches[0].code").value("ANYANG"))
                .andExpect(jsonPath("$.churches[0].name").value("안양"));
    }
}
