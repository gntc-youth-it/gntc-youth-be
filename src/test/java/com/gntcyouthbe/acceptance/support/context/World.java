package com.gntcyouthbe.acceptance.support.context;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class World {

    public String authToken;
    public ExtractableResponse<Response> response;

    public void clear() {
        authToken = null;
        response = null;
    }
}
