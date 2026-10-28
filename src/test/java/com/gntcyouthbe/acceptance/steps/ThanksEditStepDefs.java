package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.ThanksApi;
import com.gntcyouthbe.acceptance.support.context.ThanksScenarioContext;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ThanksEditStepDefs {

    private static final Long UNKNOWN_ENTRY_ID = 999_999L;

    private final World world;
    private final ThanksScenarioContext thanks;
    private final ThanksApi thanksApi;

    public ThanksEditStepDefs(World world, ThanksScenarioContext thanks, ThanksApi thanksApi) {
        this.world = world;
        this.thanks = thanks;
        this.thanksApi = thanksApi;
    }

    // 감사는 처음 올린 감사제목으로 가리킨다. 고친 뒤에도 같은 이름으로 부른다.
    @만일("{string}가 감사 {string}의 감사제목을 {string}(으)로 고친다")
    public void 감사제목을_고친다(String userName, String content, String newContent) {
        world.response = thanksApi.updateEntry(thanks.tokenOf(userName), thanks.entryIdOf(content),
                Map.of("content", newContent));
    }

    @먼저("{string}가 감사 {string}의 감사제목을 {string}(으)로 고쳤다")
    public void 감사제목을_고쳤다(String userName, String content, String newContent) {
        감사제목을_고친다(userName, content, newContent);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @만일("{string}가 감사 {string}의 감사제목을 {int}자로 고친다")
    public void 감사제목을_글자_수를_정해_고친다(String userName, String content, int length) {
        감사제목을_고친다(userName, content, "고".repeat(length));
    }

    @만일("{string}가 감사 {string}의 감사제목을 내용 없이 고친다")
    public void 감사제목을_내용_없이_고친다(String userName, String content) {
        world.response = thanksApi.updateEntry(thanks.tokenOf(userName), thanks.entryIdOf(content), Map.of());
    }

    @만일("{string}가 없는 감사의 감사제목을 {string}(으)로 고친다")
    public void 없는_감사의_감사제목을_고친다(String userName, String newContent) {
        world.response = thanksApi.updateEntry(thanks.tokenOf(userName), UNKNOWN_ENTRY_ID,
                Map.of("content", newContent));
    }

    @만일("로그인하지 않고 감사 {string}의 감사제목을 {string}(으)로 고친다")
    public void 로그인하지_않고_감사제목을_고친다(String content, String newContent) {
        world.response = thanksApi.updateEntryWithoutAuth(thanks.entryIdOf(content), Map.of("content", newContent));
    }
}
