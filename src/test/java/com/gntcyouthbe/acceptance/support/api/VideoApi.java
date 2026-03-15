package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class VideoApi {

    public ExtractableResponse<Response> createVideo(String authToken, Map<String, Object> body) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(body)
                .when().post("/videos")
                .then().extract();
    }

    public ExtractableResponse<Response> createVideoWithoutAuth(Map<String, Object> body) {
        return given()
                .contentType("application/json")
                .body(body)
                .when().post("/videos")
                .then().extract();
    }

    public ExtractableResponse<Response> getVideos(String subCategory) {
        var request = given();
        if (subCategory != null) request.queryParam("subCategory", subCategory);
        return request
                .when().get("/videos")
                .then().extract();
    }

    public ExtractableResponse<Response> deleteVideo(String authToken, Long videoId) {
        return given()
                .header("Authorization", "Bearer " + authToken)
                .when().delete("/videos/" + videoId)
                .then().extract();
    }

    public ExtractableResponse<Response> deleteVideoWithoutAuth(Long videoId) {
        return given()
                .when().delete("/videos/" + videoId)
                .then().extract();
    }
}
