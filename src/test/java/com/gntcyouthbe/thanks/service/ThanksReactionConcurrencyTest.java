package com.gntcyouthbe.thanks.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.thanks.domain.ThanksEmoji;
import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksSlot;
import com.gntcyouthbe.thanks.repository.ThanksEntryRepository;
import com.gntcyouthbe.thanks.repository.ThanksReactionRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

// 반응을 바꾸는 요청은 감사 행을 잠근 뒤 처리하므로, 같은 반응을 동시에 여러 번 눌러도 오류 없이 한 행만 남는다.
@SpringBootTest
@ActiveProfiles("test")
class ThanksReactionConcurrencyTest {

    private static final int REQUESTS = 8;

    @Autowired
    private ThanksReactionService thanksReactionService;

    @Autowired
    private ThanksEntryRepository thanksEntryRepository;

    @Autowired
    private ThanksReactionRepository thanksReactionRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UploadedFile photo;
    private ThanksEntry entry;

    @BeforeEach
    void setUp() {
        photo = uploadedFileRepository.save(new UploadedFile("thanks.webp", "stored-concurrency.webp",
                "uploads/stored-concurrency.webp", "image/webp", 1024L));
        entry = thanksEntryRepository.saveAndFlush(new ThanksEntry(userRepository.findById(1L).orElseThrow(),
                LocalDate.of(2030, 2, 1), ThanksSlot.MORNING, "동시에 누르기", photo));
    }

    @AfterEach
    void tearDown() {
        thanksReactionRepository.deleteAllInBatch();
        thanksEntryRepository.deleteById(entry.getId());
        uploadedFileRepository.deleteById(photo.getId());
    }

    @Test
    @DisplayName("같은 사람이 같은 반응을 동시에 여러 번 눌러도 모두 성공하고 반응은 하나만 남는다")
    void addSameReactionConcurrently() throws Exception {
        UserPrincipal viewer = new UserPrincipal(userRepository.findById(5L).orElseThrow(), null);
        ExecutorService executor = Executors.newFixedThreadPool(REQUESTS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();
        try {
            for (int i = 0; i < REQUESTS; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    return thanksReactionService.addReaction(viewer, entry.getId(), ThanksEmoji.PRAY);
                }));
            }
            start.countDown();
            for (Future<?> result : results) {
                result.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM thanks_reaction WHERE entry_id = ?", Long.class, entry.getId());
        assertThat(count).isEqualTo(1);
    }
}
