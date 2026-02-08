package com.gntcyouthbe.acceptance.support.api;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FileApi {

    public ExtractableResponse<Response> requestPresignedUrl(String authToken, String filename, String contentType) {
        return given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(Map.of(
                        "filename", filename,
                        "contentType", contentType
                ))
                .when().post("/files/presigned-url")
                .then().extract();
    }

    public ExtractableResponse<Response> requestPresignedUrlWithoutAuth(String filename, String contentType) {
        return given()
                .contentType("application/json")
                .body(Map.of(
                        "filename", filename,
                        "contentType", contentType
                ))
                .when().post("/files/presigned-url")
                .then().extract();
    }
}
