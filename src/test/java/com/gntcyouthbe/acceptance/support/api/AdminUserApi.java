package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
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

    private RequestSpecification givenAuth(String authToken) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken);
    }
}
