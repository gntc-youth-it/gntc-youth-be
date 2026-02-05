package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class ChurchApi {

    public ExtractableResponse<Response> getChurches() {
        return given()
                .contentType("application/json")
                .when().get("/churches")
                .then().extract();
    }
}
