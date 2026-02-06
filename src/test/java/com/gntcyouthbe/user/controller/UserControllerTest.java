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
