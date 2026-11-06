package com.gntcyouthbe.thanks.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.thanks.domain.ThanksCell;
import com.gntcyouthbe.thanks.domain.ThanksCellMember;
import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksSlot;
import com.gntcyouthbe.thanks.model.response.ThanksCellStatsResponse;
import com.gntcyouthbe.thanks.repository.ThanksCellMemberRepository;
import com.gntcyouthbe.thanks.repository.ThanksCellRepository;
import com.gntcyouthbe.thanks.repository.ThanksEntryRepository;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// FE가 보는 동안 30초마다 부르는 API라서, 구역원이 늘어도 쿼리가 늘지 않는지 실제 쿼리 수로 확인한다.
// 프로필 사진이 있는 사람(3), 프로필만 있는 사람(2), 프로필이 없는 사람(1, 4, 5)을 섞었다.
@SpringBootTest
@ActiveProfiles("test")
class ThanksCellStatsQueryCountTest {

    private static final int EXPECTED_QUERIES = 4;
    private static final List<Long> USER_IDS = List.of(1L, 2L, 3L, 4L, 5L);

    @Autowired
    private ThanksCellService thanksCellService;

    @Autowired
    private ThanksCellRepository thanksCellRepository;

    @Autowired
    private ThanksCellMemberRepository thanksCellMemberRepository;

    @Autowired
    private ThanksEntryRepository thanksEntryRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final List<UploadedFile> photos = new ArrayList<>();
    private Statistics statistics;
    private ThanksCell cell;
    private UserPrincipal viewer;

    @BeforeEach
    void setUp() {
        clearCells();
        thanksEntryRepository.deleteAllInBatch();
        cell = thanksCellRepository.save(new ThanksCell("현황구역", 1));
        viewer = new UserPrincipal(userRepository.findById(1L).orElseThrow(), null);
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
    }

    @AfterEach
    void tearDown() {
        statistics.setStatisticsEnabled(false);
        thanksEntryRepository.deleteAllInBatch();
        uploadedFileRepository.deleteAll(photos);
        clearCells();
    }

    @Test
    @DisplayName("구역원이 한 명이든 여러 명이든 쿼리는 구역 확인, 구역원과 사용자, 프로필 사진, 날짜별 개수 네 번이다")
    void queryCountStaysTheSame() {
        addMemberWithEntries(USER_IDS.getFirst());
        assertThat(queriesOfGetCellStats(1)).isEqualTo(EXPECTED_QUERIES);

        USER_IDS.stream().skip(1).forEach(this::addMemberWithEntries);
        assertThat(queriesOfGetCellStats(USER_IDS.size())).isEqualTo(EXPECTED_QUERIES);
    }

    private long queriesOfGetCellStats(int expectedMembers) {
        statistics.clear();
        ThanksCellStatsResponse response = thanksCellService.getCellStats(viewer, cell.getId());
        long queries = statistics.getPrepareStatementCount();
        assertThat(response.members()).hasSize(expectedMembers);
        return queries;
    }

    // 사람마다 날짜 두 개에 걸쳐 올린다
    private void addMemberWithEntries(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        thanksCellMemberRepository.save(new ThanksCellMember(user, cell));
        saveEntry(user, LocalDate.of(2030, 3, 1), ThanksSlot.MORNING);
        saveEntry(user, LocalDate.of(2030, 3, 2), ThanksSlot.EVENING);
    }

    private void saveEntry(User user, LocalDate date, ThanksSlot slot) {
        String name = "stats-" + user.getId() + "-" + date + "-" + slot;
        UploadedFile photo = uploadedFileRepository.save(
                new UploadedFile(name + ".webp", name + ".webp", "uploads/" + name + ".webp", "image/webp", 1024L));
        photos.add(photo);
        thanksEntryRepository.save(new ThanksEntry(user, date, slot, "감사합니다", photo));
    }

    private void clearCells() {
        thanksCellMemberRepository.deleteAllInBatch();
        thanksCellRepository.deleteAllInBatch();
    }
}
