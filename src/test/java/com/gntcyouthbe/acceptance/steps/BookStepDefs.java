package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.BookApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import org.springframework.http.HttpStatus;

public class BookStepDefs {

    private final World world;
    private final BookApi bookApi;

    public BookStepDefs(World world, BookApi bookApi) {
        this.world = world;
        this.bookApi = bookApi;
    }

    @먼저("성경 목록 API가 존재한다")
    public void 성경_목록_api가_존재한다() {
        // API endpoint exists - no action needed
    }

    @만일("사용자가 성경 목록을 조회한다")
    public void 사용자가_성경_목록을_조회한다() {
        world.response = bookApi.getBooks();
    }

    @그러면("성경 {int}권의 목록이 반환된다")
    public void 성경_n권의_목록이_반환된다(int count) {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getList("books")).hasSize(count);
    }

    @만일("사용자가 {string} 책의 장 목록을 조회한다")
    public void 사용자가_책의_장_목록을_조회한다(String bookName) {
        world.response = bookApi.getChapters(bookName);
    }

    @그러면("해당 책의 장 개수가 반환된다")
    public void 해당_책의_장_개수가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getInt("chapters")).isGreaterThan(0);
    }

    @만일("사용자가 {string} 책의 {int}장 내용을 조회한다")
    public void 사용자가_책의_n장_내용을_조회한다(String bookName, int chapter) {
        world.response = bookApi.getVerses(bookName, chapter);
    }

    @그러면("해당 장의 절 목록이 반환된다")
    public void 해당_장의_절_목록이_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getList("verses")).isNotEmpty();
    }

    @만일("사용자가 존재하지 않는 {string} 책을 조회한다")
    public void 사용자가_존재하지_않는_책을_조회한다(String bookName) {
        world.response = bookApi.getChapters(bookName);
    }

    @그러면("에러 응답이 반환된다")
    public void 에러_응답이_반환된다() {
        assertThat(world.response.statusCode()).isGreaterThanOrEqualTo(400);
    }

    @그러면("빈 절 목록이 반환된다")
    public void 빈_절_목록이_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getList("verses")).isEmpty();
    }
}
