package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.cell.domain.CellGoal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChapterListResponse {

    private final int chapters;

    private final List<Integer> missionChapters;

    private final List<Integer> completedChapters;

    public ChapterListResponse(
            final CellGoal goal,
            final BookName targetBook,
            final List<Integer> completedChapters
    ) {
        this.chapters = targetBook.getChapters();
        this.completedChapters = completedChapters;

        int startOrder   = goal.getStartBookOrder();
        int startChapter = goal.getStartChapter();
        int endOrder     = goal.getEndBookOrder();
        int endChapter   = goal.getEndChapter();

        // --- 구간 정규화 (역순 입력 대비) ---
        if (startOrder > endOrder || (startOrder == endOrder && startChapter > endChapter)) {
            int tmpOrder = startOrder;   startOrder = endOrder;   endOrder = tmpOrder;
            int tmpChap  = startChapter; startChapter = endChapter; endChapter = tmpChap;
        }

        // --- 대상 책과의 겹침 계산 ---
        final int targetOrder = targetBook.getOrder();
        final int totalChapters = targetBook.getChapters();
        final List<Integer> result = new ArrayList<>();

        // 전혀 겹치지 않음
        if (targetOrder < startOrder || targetOrder > endOrder) {
            this.missionChapters = result;
            return;
        }

        // 시작과 끝이 같은 책(=대상 책) 안에 모두 포함
        if (startOrder == endOrder && targetOrder == startOrder) {
            int from = Math.max(1, Math.min(startChapter, endChapter));
            int to   = Math.min(totalChapters, Math.max(startChapter, endChapter));
            IntStream.rangeClosed(from, to).forEach(result::add);
            this.missionChapters = result;
            return;
        }

        // 시작 책인 경우: 시작장 ~ 끝까지
        if (targetOrder == startOrder) {
            int from = Math.max(1, startChapter);
            IntStream.rangeClosed(from, totalChapters).forEach(result::add);
            this.missionChapters = result;
            return;
        }

        // 끝 책인 경우: 1장 ~ 끝장
        if (targetOrder == endOrder) {
            int to = Math.min(totalChapters, endChapter);
            IntStream.rangeClosed(1, to).forEach(result::add);
            this.missionChapters = result;
            return;
        }

        // 사이에 낀 책: 전체 장이 미션
        IntStream.rangeClosed(1, totalChapters).forEach(result::add);
        this.missionChapters = result;
    }
}
