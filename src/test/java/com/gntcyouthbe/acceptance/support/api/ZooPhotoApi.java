package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;

@Component
public class ZooPhotoApi {

    public ExtractableResponse<Response> getPhotos(String authToken) {
        return authorized(authToken)
                .when().get("/zoo/photos")
                .then().extract();
    }

    public ExtractableResponse<Response> vote(String authToken, Long photoId) {
        return authorized(authToken)
                .when().put("/zoo/photos/" + photoId + "/vote")
                .then().extract();
    }

    public ExtractableResponse<Response> cancelVote(String authToken, Long photoId) {
        return authorized(authToken)
                .when().delete("/zoo/photos/" + photoId + "/vote")
                .then().extract();
    }

    public ExtractableResponse<Response> getResults(String authToken) {
        return authorized(authToken)
                .when().get("/zoo/photos/results")
                .then().extract();
    }

    private RequestSpecification authorized(String authToken) {
        return given().header("Authorization", "Bearer " + authToken);
    }
}
