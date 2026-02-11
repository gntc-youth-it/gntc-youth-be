package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class AdminUserApi {

    public ExtractableResponse<Response> getAllUsers(String authToken) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .when().get("/admin/users")
                .then().extract();
    }

    public ExtractableResponse<Response> getAllUsersWithoutAuth() {
        return given()
                .contentType("application/json")
                .when().get("/admin/users")
                .then().extract();
    }
}
