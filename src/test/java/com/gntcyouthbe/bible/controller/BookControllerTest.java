package com.gntcyouthbe.bible.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.bible.model.response.BookListResponse;
import com.gntcyouthbe.bible.model.response.BookListResponse.BookListItem;
import com.gntcyouthbe.bible.model.response.ChapterListResponse;
import com.gntcyouthbe.bible.model.response.ChapterResponse;
import com.gntcyouthbe.bible.model.response.ChapterResponse.VerseItem;
import com.gntcyouthbe.bible.service.BookService;
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

// SecurityConfig 제외: OAuth2 로그인·JWT 필터 등 보안 의존성이 @WebMvcTest 슬라이스에 포함되면
// CustomOAuth2UserService, FrontendProperties 등 컨트롤러와 무관한 빈을 모두 mock 해야 하므로 제외한다.
// OAuth2 자동 구성 제외: Spring Boot 4에서 @WebMvcTest가 OAuth2 자동 구성을 포함하지만
// SecurityAutoConfiguration은 포함하지 않아 HttpSecurity 빈이 없어 컨텍스트 로드에 실패한다.
@WebMvcTest(value = BookController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        excludeAutoConfiguration = {OAuth2ClientAutoConfiguration.class, OAuth2ClientWebSecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /book - 성경 목록을 반환한다")
    void getBookList() throws Exception {
        given(bookService.getBookList()).willReturn(
                new BookListResponse(List.of(
                        new BookListItem("GENESIS", "창세기", 1)
                )));

        mockMvc.perform(get("/book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books[0].bookCode").value("GENESIS"))
                .andExpect(jsonPath("$.books[0].bookName").value("창세기"));
    }

    @Test
    @DisplayName("GET /book/{bookName} - 장 수를 반환한다")
    void getChapterList() throws Exception {
        given(bookService.getChapterList(BookName.GENESIS)).willReturn(
                new ChapterListResponse(50));

        mockMvc.perform(get("/book/GENESIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chapters").value(50));
    }

    @Test
    @DisplayName("GET /book/{bookName} - 유효하지 않은 bookName이면 400을 반환한다")
    void getChapterListInvalidBookName() throws Exception {
        mockMvc.perform(get("/book/INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /book/{bookName}/{chapter} - 구절 목록을 반환한다")
    void getChapterVerses() throws Exception {
        ChapterResponse chapterResponse = mock(ChapterResponse.class);
        given(chapterResponse.getVerses()).willReturn(
                List.of(new VerseItem(1L, 1, "태초에 하나님이 천지를 창조하시니라")));
        given(bookService.getChapterVerses(BookName.GENESIS, 1)).willReturn(chapterResponse);

        mockMvc.perform(get("/book/GENESIS/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verses").isArray());
    }

    @Test
    @DisplayName("GET /book/{bookName}/{chapter} - 존재하지 않는 장이면 404를 반환한다")
    void getChapterVersesNotFound() throws Exception {
        given(bookService.getChapterVerses(BookName.GENESIS, 999))
                .willThrow(new EntityNotFoundException(ExceptionCode.CHAPTER_NOT_FOUND));

        mockMvc.perform(get("/book/GENESIS/999"))
                .andExpect(status().isNotFound());
    }
}
