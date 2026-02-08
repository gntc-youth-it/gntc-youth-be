package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UserProfileApi {

    public ExtractableResponse<Response> saveProfile(String authToken, int generation, String phoneNumber, String gender) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(Map.of(
                        "generation", generation,
                        "phoneNumber", phoneNumber,
                        "gender", gender
                ))
                .when().put("/user/profile")
                .then().extract();
    }

    public ExtractableResponse<Response> getProfile(String authToken) {
        return given()
                .header("Authorization", "Bearer " + authToken)
                .when().get("/user/profile")
                .then().extract();
    }

    public ExtractableResponse<Response> saveProfileWithoutAuth(int generation, String phoneNumber, String gender) {
        return given()
                .contentType("application/json")
                .body(Map.of(
                        "generation", generation,
                        "phoneNumber", phoneNumber,
                        "gender", gender
                ))
                .when().put("/user/profile")
                .then().extract();
    }
}
