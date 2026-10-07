package com.gntcyouthbe.zoo.model.response;

// 속한 조가 없으면 team은 null이다
public record MyZooTeamResponse(
        ZooTeamDetailResponse team
) {
}
