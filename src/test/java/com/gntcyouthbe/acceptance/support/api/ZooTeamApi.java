package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ZooTeamApi {

    public ExtractableResponse<Response> getTeams(String authToken) {
        return authorized(authToken)
                .when().get("/zoo/teams")
                .then().extract();
    }

    public ExtractableResponse<Response> getTeamsWithoutAuth() {
        return given()
                .when().get("/zoo/teams")
                .then().extract();
    }

    public ExtractableResponse<Response> getMyTeam(String authToken) {
        return authorized(authToken)
                .when().get("/zoo/teams/me")
                .then().extract();
    }

    public ExtractableResponse<Response> getTeam(String authToken, Long teamId) {
        return authorized(authToken)
                .when().get("/zoo/teams/" + teamId)
                .then().extract();
    }

    public ExtractableResponse<Response> createTeam(String authToken, String name, String course) {
        return authorized(authToken)
                .contentType("application/json")
                .body(Map.of("name", name, "course", course))
                .when().post("/zoo/teams")
                .then().extract();
    }

    public ExtractableResponse<Response> createTeamWithoutAuth(String name, String course) {
        return given()
                .contentType("application/json")
                .body(Map.of("name", name, "course", course))
                .when().post("/zoo/teams")
                .then().extract();
    }

    public ExtractableResponse<Response> joinTeam(String authToken, Long teamId) {
        return authorized(authToken)
                .when().post("/zoo/teams/" + teamId + "/join")
                .then().extract();
    }

    public ExtractableResponse<Response> leaveTeam(String authToken, Long teamId) {
        return authorized(authToken)
                .when().post("/zoo/teams/" + teamId + "/leave")
                .then().extract();
    }

    public ExtractableResponse<Response> deleteTeam(String authToken, Long teamId) {
        return authorized(authToken)
                .when().delete("/zoo/teams/" + teamId)
                .then().extract();
    }

    public ExtractableResponse<Response> changeCourse(String authToken, Long teamId, String course) {
        return authorized(authToken)
                .contentType("application/json")
                .body(Map.of("course", course))
                .when().patch("/zoo/teams/" + teamId)
                .then().extract();
    }

    public ExtractableResponse<Response> startTeam(String authToken, Long teamId) {
        return authorized(authToken)
                .when().post("/zoo/teams/" + teamId + "/start")
                .then().extract();
    }

    public ExtractableResponse<Response> markArrival(String authToken, Long teamId, String stopId) {
        return authorized(authToken)
                .when().put("/zoo/teams/" + teamId + "/arrivals/" + stopId)
                .then().extract();
    }

    public ExtractableResponse<Response> cancelArrival(String authToken, Long teamId, String stopId) {
        return authorized(authToken)
                .when().delete("/zoo/teams/" + teamId + "/arrivals/" + stopId)
                .then().extract();
    }

    public ExtractableResponse<Response> transferLeader(String authToken, Long teamId, Long userId) {
        return authorized(authToken)
                .contentType("application/json")
                .body(Map.of("userId", userId))
                .when().post("/zoo/teams/" + teamId + "/leader")
                .then().extract();
    }

    private RequestSpecification authorized(String authToken) {
        return given().header("Authorization", "Bearer " + authToken);
    }
}
