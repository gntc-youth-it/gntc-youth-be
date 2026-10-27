package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UserProfileApi {

    public ExtractableResponse<Response> saveProfile(
            String authToken, String name, String churchId, int generation, String phoneNumber, String gender) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(Map.of(
                        "name", name,
                        "churchId", churchId,
                        "generation", generation,
                        "phoneNumber", phoneNumber,
                        "gender", gender
                ))
                .when().put("/user/profile")
                .then().extract();
    }

    public ExtractableResponse<Response> saveProfile(String authToken, Map<String, Object> body) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(body)
                .when().put("/user/profile")
                .then().extract();
    }

    public ExtractableResponse<Response> updateProfile(String authToken, Map<String, Object> body) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(body)
                .when().patch("/user/profile")
                .then().extract();
    }

    public ExtractableResponse<Response> updateProfileWithoutAuth(Map<String, Object> body) {
        return given()
                .contentType("application/json")
                .body(body)
                .when().patch("/user/profile")
                .then().extract();
    }

    public ExtractableResponse<Response> getProfile(String authToken) {
        return given()
                .header("Authorization", "Bearer " + authToken)
                .when().get("/user/profile")
                .then().extract();
    }

    public ExtractableResponse<Response> saveProfileWithoutAuth(
            String name, String churchId, int generation, String phoneNumber, String gender) {
        return given()
                .contentType("application/json")
                .body(Map.of(
                        "name", name,
                        "churchId", churchId,
                        "generation", generation,
                        "phoneNumber", phoneNumber,
                        "gender", gender
                ))
                .when().put("/user/profile")
                .then().extract();
    }
}
