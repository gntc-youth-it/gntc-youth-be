package com.gntcyouthbe.thanks.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksSlot;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

// ThanksEntryService는 unique 위반만 6002로 바꾸고 나머지 무결성 오류는 그대로 던진다.
// 그 구분이 실제 DB에서도 되는지, Spring과 Hibernate가 주는 예외 모양을 확인한다.
@SpringBootTest
@ActiveProfiles("test")
class ThanksEntryRepositoryTest {

    private static final LocalDate MISSION_DATE = LocalDate.of(2030, 1, 1);

    @Autowired
    private ThanksEntryRepository thanksEntryRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User user;
    private UploadedFile firstPhoto;
    private UploadedFile secondPhoto;
    private ThanksEntry lunchEntry;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(1L).orElseThrow();
        firstPhoto = uploadedFileRepository.save(createPhoto("first"));
        secondPhoto = uploadedFileRepository.save(createPhoto("second"));
        lunchEntry = thanksEntryRepository.saveAndFlush(
                new ThanksEntry(user, MISSION_DATE, ThanksSlot.AFTERNOON, "점심 감사", firstPhoto));
    }

    @AfterEach
    void tearDown() {
        thanksEntryRepository.deleteAllInBatch();
        uploadedFileRepository.deleteAll(List.of(firstPhoto, secondPhoto));
    }

    @Test
    @DisplayName("같은 사람·날짜·칸으로 또 저장하면 UNIQUE 위반이 Hibernate 예외에 담겨 온다")
    void saveSameSlot() {
        assertThatThrownBy(() -> thanksEntryRepository.saveAndFlush(
                new ThanksEntry(user, MISSION_DATE, ThanksSlot.AFTERNOON, "또 올리는 감사", secondPhoto)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .cause()
                .isInstanceOfSatisfying(ConstraintViolationException.class,
                        violation -> assertThat(violation.getKind()).isEqualTo(ConstraintKind.UNIQUE));
    }

    @Test
    @DisplayName("이미 붙인 파일을 다른 칸에 또 붙여도 UNIQUE 위반으로 온다")
    void saveSameFile() {
        assertThatThrownBy(() -> thanksEntryRepository.saveAndFlush(
                new ThanksEntry(user, MISSION_DATE, ThanksSlot.EVENING, "저녁 감사", firstPhoto)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .cause()
                .isInstanceOfSatisfying(ConstraintViolationException.class,
                        violation -> assertThat(violation.getKind()).isEqualTo(ConstraintKind.UNIQUE));
    }

    @Test
    @DisplayName("NOT NULL 위반은 UNIQUE 위반으로 오지 않는다")
    void saveWithoutContent() {
        ThanksEntry entry = new ThanksEntry(user, MISSION_DATE, ThanksSlot.MORNING, "아침 감사", secondPhoto);
        ReflectionTestUtils.setField(entry, "content", null);

        assertThatThrownBy(() -> thanksEntryRepository.saveAndFlush(entry))
                .isInstanceOf(DataIntegrityViolationException.class)
                .cause()
                .isInstanceOfSatisfying(ConstraintViolationException.class,
                        violation -> assertThat(violation.getKind()).isEqualTo(ConstraintKind.NOT_NULL));
    }

    // ThanksEntryService.updateEntry는 감사 행을 잠그지 않고, 이 예외를 6003으로 바꾼다
    @Test
    @DisplayName("읽은 뒤 다른 트랜잭션이 지운 감사를 고치면 flush할 때 OptimisticLockingFailureException이 난다")
    void updateDeletedEntry() {
        transactionTemplate.executeWithoutResult(status -> {
            ThanksEntry entry = thanksEntryRepository.findById(lunchEntry.getId()).orElseThrow();
            entry.updateContent("고친 감사");
            CompletableFuture.runAsync(() ->
                    jdbcTemplate.update("DELETE FROM thanks_entry WHERE id = ?", lunchEntry.getId())).join();

            assertThatThrownBy(thanksEntryRepository::flush).isInstanceOf(OptimisticLockingFailureException.class);
            status.setRollbackOnly();
        });
    }

    private UploadedFile createPhoto(String name) {
        return new UploadedFile(name + ".webp", "stored-" + name + ".webp", "uploads/stored-" + name + ".webp",
                "image/webp", 1024L);
    }
}
