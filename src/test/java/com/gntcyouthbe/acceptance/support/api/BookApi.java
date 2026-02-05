package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class BookApi {

    public ExtractableResponse<Response> getBooks() {
        return given()
                .contentType("application/json")
                .when().get("/book")
                .then().extract();
    }

    public ExtractableResponse<Response> getChapters(String bookName) {
        return given()
                .contentType("application/json")
                .when().get("/book/{bookName}", bookName)
                .then().extract();
    }

    public ExtractableResponse<Response> getVerses(String bookName, int chapter) {
        return given()
                .contentType("application/json")
                .when().get("/book/{bookName}/{chapter}", bookName, chapter)
                .then().extract();
    }
}
