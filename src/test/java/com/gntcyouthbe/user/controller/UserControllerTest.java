package com.gntcyouthbe.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gntcyouthbe.common.security.configuration.SecurityConfig;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.common.security.service.JwtService;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// SecurityConfig 제외: OAuth2 로그인·JWT 필터 등 보안 의존성이 @WebMvcTest 슬라이스에 포함되면
// CustomOAuth2UserService, FrontendProperties 등 컨트롤러와 무관한 빈을 모두 mock 해야 하므로 제외한다.
// @ImportAutoConfiguration: @AuthenticationPrincipal 해석에 필요한 SecurityAutoConfiguration을 명시적으로 포함한다.
// (Spring Boot 4의 @WebMvcTest는 기본적으로 SecurityAutoConfiguration을 포함하지 않는다.)
@WebMvcTest(value = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@ImportAutoConfiguration({SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("PUT /user/name - 이름 변경 성공 시 200을 반환한다")
    void updateUserNameSuccess() throws Exception {
        willDoNothing().given(userService).updateUserName(any(), any());

        UserPrincipal principal = new UserPrincipal(1L, "test@example.com", "테스트", Role.USER, null, null);

        mockMvc.perform(put("/user/name")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"new_name\": \"새이름\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이름 변경이 성공적으로 완료되었습니다."));
    }

    @Test
    @DisplayName("PUT /user/name - 빈 이름이면 400을 반환한다")
    void updateUserNameBlankValidation() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "test@example.com", "테스트", Role.USER, null, null);

        mockMvc.perform(put("/user/name")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"new_name\": \"\"}"))
                .andExpect(status().isBadRequest());
    }
}
