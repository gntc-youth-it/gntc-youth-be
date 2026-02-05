package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AuthApi {

    public ExtractableResponse<Response> testLogin(String email) {
        return given()
                .contentType("application/json")
                .body(Map.of("email", email))
                .when().post("/api/auth/test/login")
                .then().extract();
    }

    public String getAccessToken(String email) {
        return testLogin(email).jsonPath().getString("accessToken");
    }
}
