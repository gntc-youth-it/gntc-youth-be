package com.gntcyouthbe.zoo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 문제와 정답은 프론트에 있고, 서버는 questionId별 답만 저장한다. 채점은 행사 후 운영진이 한다.
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZooMissionAnswer {

    @Column(name = "question_id", nullable = false, length = 20)
    private String questionId;

    @Column(nullable = false, length = 200)
    private String answer;

    public ZooMissionAnswer(String questionId, String answer) {
        this.questionId = questionId;
        this.answer = answer;
    }
}
