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
}
