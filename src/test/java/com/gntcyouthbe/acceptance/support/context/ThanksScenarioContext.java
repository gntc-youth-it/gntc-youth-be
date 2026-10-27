package com.gntcyouthbe.acceptance.support.context;

import com.gntcyouthbe.acceptance.support.api.AuthApi;
import io.cucumber.spring.ScenarioScope;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

// 감사로그 시나리오가 함께 쓰는 상태. 시나리오마다 새로 만들어진다.
@Component
@ScenarioScope
public class ThanksScenarioContext {

    // 새내기유저는 data.sql에 없고, 감사로그 프로필 시나리오(ThanksProfileStepDefs)가 그때그때 만든다
    private static final Map<String, String> EMAILS = Map.of(
            "테스트유저", "test@example.com",
            "리더유저", "leader@example.com",
            "마스터유저", "master@example.com",
            "수원유저", "suwon-user@example.com",
            "안양유저", "anyang-user@example.com",
            "담당자유저", "manager@example.com",
            "새내기유저", "newcomer@example.com"
    );

    private final AuthApi authApi;
    private final Map<String, String> tokens = new HashMap<>();
    private final Map<String, String> refreshTokens = new HashMap<>();
    private final Map<String, Long> userIds = new HashMap<>();
    private final Map<String, String> userNames = new HashMap<>();
    private final Map<String, Long> fileIds = new HashMap<>();
    private final Map<String, Long> entryIds = new HashMap<>();

    public ThanksScenarioContext(AuthApi authApi) {
        this.authApi = authApi;
    }

    public String tokenOf(String userName) {
        return tokens.computeIfAbsent(userName, name -> {
            String email = Objects.requireNonNull(EMAILS.get(name), "테스트 데이터에 없는 사용자: " + name);
            ExtractableResponse<Response> response = authApi.testLogin(email);
            userIds.put(name, response.jsonPath().getLong("userId"));
            userNames.put(name, response.jsonPath().getString("name"));
            refreshTokens.put(name, response.cookie("refreshToken"));
            return response.jsonPath().getString("accessToken");
        });
    }

    // 로그인할 때 쿠키로 받은 refresh token
    public String refreshTokenOf(String userName) {
        tokenOf(userName);
        return refreshTokens.get(userName);
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

    public void saveFileId(String fileName, Long fileId) {
        fileIds.put(fileName, fileId);
    }

    public Long fileIdOf(String fileName) {
        return Objects.requireNonNull(fileIds.get(fileName), "시나리오에서 업로드하지 않은 파일: " + fileName);
    }

    // 감사는 시나리오 안에서 감사제목으로 구분한다
    public void saveEntryId(String content, Long entryId) {
        entryIds.put(content, entryId);
    }

    public Long entryIdOf(String content) {
        return Objects.requireNonNull(entryIds.get(content), "시나리오에서 올리지 않은 감사: " + content);
    }
}
