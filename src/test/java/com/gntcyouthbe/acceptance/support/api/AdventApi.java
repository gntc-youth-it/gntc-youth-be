package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class AdventApi {

    public ExtractableResponse<Response> getAdventVerses(String name, String temple, int batch) {
        return given()
                .contentType("application/json")
                .queryParam("name", name)
                .queryParam("temple", temple)
                .queryParam("batch", batch)
                .when().get("/advent")
                .then().extract();
    }

    public ExtractableResponse<Response> getAdventVersesWithoutParams() {
        return given()
                .contentType("application/json")
                .when().get("/advent")
                .then().extract();
    }
}
