package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ChurchInfoApi {

    public ExtractableResponse<Response> saveChurchInfo(
            String authToken, String churchId, Long groupPhotoFileId, List<Map<String, Object>> prayerTopics) {
        Map<String, Object> body = new java.util.HashMap<>();
        if (groupPhotoFileId != null) {
            body.put("groupPhotoFileId", groupPhotoFileId);
        }
        body.put("prayerTopics", prayerTopics);

        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(body)
                .when().put("/churches/" + churchId + "/info")
                .then().extract();
    }

    public ExtractableResponse<Response> saveChurchInfoWithoutAuth(
            String churchId, List<Map<String, Object>> prayerTopics) {
        return given()
                .contentType("application/json")
                .body(Map.of("prayerTopics", prayerTopics))
                .when().put("/churches/" + churchId + "/info")
                .then().extract();
    }

    public ExtractableResponse<Response> getChurchInfo(String churchId) {
        return given()
                .contentType("application/json")
                .when().get("/churches/" + churchId + "/info")
                .then().extract();
    }
}
