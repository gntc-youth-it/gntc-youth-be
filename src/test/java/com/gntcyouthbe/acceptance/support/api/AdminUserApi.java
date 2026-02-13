package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AdminUserApi {

    public ExtractableResponse<Response> getUsers(String authToken, int page, int size) {
        return givenAuth(authToken)
                .queryParam("page", page)
                .queryParam("size", size)
                .when().get("/admin/users")
                .then().extract();
    }

    public ExtractableResponse<Response> getUsersByName(String authToken, int page, int size, String name) {
        return givenAuth(authToken)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("name", name)
                .when().get("/admin/users")
                .then().extract();
    }

    public ExtractableResponse<Response> getUsersWithoutAuth() {
        return given()
                .contentType("application/json")
                .when().get("/admin/users")
                .then().extract();
    }

    public ExtractableResponse<Response> updateUserRole(String authToken, long userId, String role) {
        return givenAuth(authToken)
                .body(Map.of("role", role))
                .when().patch("/admin/users/{userId}/role", userId)
                .then().extract();
    }

    public ExtractableResponse<Response> updateUserRoleWithoutAuth(long userId, String role) {
        return given()
                .contentType("application/json")
                .body(Map.of("role", role))
                .when().patch("/admin/users/{userId}/role", userId)
                .then().extract();
    }

    private RequestSpecification givenAuth(String authToken) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken);
    }
}
