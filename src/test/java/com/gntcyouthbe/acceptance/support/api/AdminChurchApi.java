package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class AdminChurchApi {

    public ExtractableResponse<Response> getChurchLeader(String authToken, String churchId) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .when().get("/admin/churches/{churchId}/leader", churchId)
                .then().extract();
    }

    public ExtractableResponse<Response> getChurchLeaderWithoutAuth(String churchId) {
        return given()
                .contentType("application/json")
                .when().get("/admin/churches/{churchId}/leader", churchId)
                .then().extract();
    }
}
