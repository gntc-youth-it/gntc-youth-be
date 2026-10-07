package com.gntcyouthbe.acceptance.support.context;

import com.gntcyouthbe.acceptance.support.api.AuthApi;
import io.cucumber.spring.ScenarioScope;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

// 동물원 조·미션 시나리오가 함께 쓰는 상태. 시나리오마다 새로 만들어진다.
@Component
@ScenarioScope
public class ZooScenarioContext {

    private static final Map<String, String> EMAILS = Map.of(
            "테스트유저", "test@example.com",
            "리더유저", "leader@example.com",
            "마스터유저", "master@example.com",
            "수원유저", "suwon-user@example.com",
            "안양유저", "anyang-user@example.com",
            "담당자유저", "manager@example.com"
    );

    private final AuthApi authApi;
    private final Map<String, String> tokens = new HashMap<>();
    private final Map<String, Long> userIds = new HashMap<>();
    private final Map<String, String> userNames = new HashMap<>();
    private final Map<String, Long> teamIds = new HashMap<>();

    public ZooScenarioContext(AuthApi authApi) {
        this.authApi = authApi;
    }

    public String tokenOf(String userName) {
        return tokens.computeIfAbsent(userName, name -> {
            String email = Objects.requireNonNull(EMAILS.get(name), "테스트 데이터에 없는 사용자: " + name);
            ExtractableResponse<Response> response = authApi.testLogin(email);
            userIds.put(name, response.jsonPath().getLong("userId"));
            userNames.put(name, response.jsonPath().getString("name"));
            return response.jsonPath().getString("accessToken");
        });
    }

    public Long userIdOf(String userName) {
        tokenOf(userName);
        return userIds.get(userName);
    }

    // 다른 기능 테스트가 이름을 바꿀 수 있어서 로그인 응답의 현재 이름을 쓴다
    public String currentNameOf(String userName) {
        tokenOf(userName);
        return userNames.get(userName);
    }

    public void saveTeamId(String teamName, Long teamId) {
        teamIds.put(teamName, teamId);
    }

    public Long teamIdOf(String teamName) {
        return Objects.requireNonNull(teamIds.get(teamName), "시나리오에서 만들지 않은 조: " + teamName);
    }
}
