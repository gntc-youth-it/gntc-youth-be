package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PostApi {

    public ExtractableResponse<Response> createPost(String authToken, Map<String, Object> body) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(body)
                .when().post("/posts")
                .then().extract();
    }

    public ExtractableResponse<Response> createPostWithoutAuth(Map<String, Object> body) {
        return given()
                .contentType("application/json")
                .body(body)
                .when().post("/posts")
                .then().extract();
    }

    public ExtractableResponse<Response> getSubCategories(String category) {
        return given()
                .when().get("/posts/categories/" + category + "/sub-categories")
                .then().extract();
    }

    public ExtractableResponse<Response> getGallery(String subCategory, String churchId, Long cursor,
            Integer size) {
        var request = given();
        if (subCategory != null) request.queryParam("subCategory", subCategory);
        if (churchId != null) request.queryParam("churchId", churchId);
        if (cursor != null) request.queryParam("cursor", cursor);
        if (size != null) request.queryParam("size", size);
        return request
                .when().get("/posts/gallery")
                .then().extract();
    }

    public ExtractableResponse<Response> deletePost(String authToken, Long postId) {
        return given()
                .header("Authorization", "Bearer " + authToken)
                .when().delete("/posts/" + postId)
                .then().extract();
    }

    public ExtractableResponse<Response> deletePostWithoutAuth(Long postId) {
        return given()
                .when().delete("/posts/" + postId)
                .then().extract();
    }

    public ExtractableResponse<Response> getFeed(String subCategory, String churchId, Long cursor,
            Integer size) {
        var request = given();
        if (subCategory != null) request.queryParam("subCategory", subCategory);
        if (churchId != null) request.queryParam("churchId", churchId);
        if (cursor != null) request.queryParam("cursor", cursor);
        if (size != null) request.queryParam("size", size);
        return request
                .when().get("/posts/feed")
                .then().extract();
    }
}
