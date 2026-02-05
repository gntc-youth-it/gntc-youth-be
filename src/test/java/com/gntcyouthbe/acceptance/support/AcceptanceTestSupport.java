package com.gntcyouthbe.acceptance.support;

import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AcceptanceTestSupport {

    @LocalServerPort
    private int port;

    @Autowired
    private World world;

    @Before
    public void setUp() {
        RestAssured.port = port;
    }

    @After
    public void tearDown() {
        world.clear();
    }
}
