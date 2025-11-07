package com.gntcyouthbe.bible.service;

import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.bible.model.response.RecentRankResponse;
import com.gntcyouthbe.bible.repository.VerseCopyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankService {

    private final VerseCopyRepository copyRepository;

    @Transactional(readOnly = true)
    public RecentRankResponse getRecentRank() {
        List<VerseCopy> recentCopies = copyRepository.findLatestPerUserOrderByCreatedAtDescLimited();
        return new RecentRankResponse(recentCopies);
    }
}
