package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ThanksApi {

    public ExtractableResponse<Response> getEntries(String authToken, String date) {
        return authorized(authToken)
                .queryParam("date", date)
                .when().get("/thanks/entries")
                .then().extract();
    }

    public ExtractableResponse<Response> getEntriesWithoutDate(String authToken) {
        return authorized(authToken)
                .when().get("/thanks/entries")
                .then().extract();
    }

    public ExtractableResponse<Response> getEntriesWithoutAuth(String date) {
        return given()
                .queryParam("date", date)
                .when().get("/thanks/entries")
                .then().extract();
    }

    public ExtractableResponse<Response> createEntry(String authToken, Map<String, Object> body) {
        return authorized(authToken)
                .contentType("application/json")
                .body(body)
                .when().post("/thanks/entries")
                .then().extract();
    }

    public ExtractableResponse<Response> createEntryWithoutAuth(Map<String, Object> body) {
        return given()
                .contentType("application/json")
                .body(body)
                .when().post("/thanks/entries")
                .then().extract();
    }

    public ExtractableResponse<Response> deleteEntry(String authToken, Long entryId) {
        return authorized(authToken)
                .when().delete("/thanks/entries/" + entryId)
                .then().extract();
    }

    public ExtractableResponse<Response> deleteEntryWithoutAuth(Long entryId) {
        return given()
                .when().delete("/thanks/entries/" + entryId)
                .then().extract();
    }

    public ExtractableResponse<Response> addReaction(String authToken, Long entryId, String emoji) {
        return authorized(authToken)
                .when().put("/thanks/entries/" + entryId + "/reactions/" + emoji)
                .then().extract();
    }

    public ExtractableResponse<Response> addReactionWithoutAuth(Long entryId, String emoji) {
        return given()
                .when().put("/thanks/entries/" + entryId + "/reactions/" + emoji)
                .then().extract();
    }

    public ExtractableResponse<Response> removeReaction(String authToken, Long entryId, String emoji) {
        return authorized(authToken)
                .when().delete("/thanks/entries/" + entryId + "/reactions/" + emoji)
                .then().extract();
    }

    public ExtractableResponse<Response> removeReactionWithoutAuth(Long entryId, String emoji) {
        return given()
                .when().delete("/thanks/entries/" + entryId + "/reactions/" + emoji)
                .then().extract();
    }

    private RequestSpecification authorized(String authToken) {
        return given().header("Authorization", "Bearer " + authToken);
    }
}
