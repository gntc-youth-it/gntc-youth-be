package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UserApi {

    public ExtractableResponse<Response> updateName(String authToken, String newName) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(Map.of("newName", newName))
                .when().put("/user/name")
                .then().extract();
    }

    public ExtractableResponse<Response> updateNameWithoutAuth(String newName) {
        return given()
                .contentType("application/json")
                .body(Map.of("newName", newName))
                .when().put("/user/name")
                .then().extract();
    }
}
