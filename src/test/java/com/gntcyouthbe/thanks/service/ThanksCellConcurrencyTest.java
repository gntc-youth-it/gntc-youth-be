package com.gntcyouthbe.thanks.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.thanks.domain.ThanksCell;
import com.gntcyouthbe.thanks.domain.ThanksCellMember;
import com.gntcyouthbe.thanks.model.request.ThanksMyCellUpdateRequest;
import com.gntcyouthbe.thanks.model.response.ThanksCellListResponse;
import com.gntcyouthbe.thanks.repository.ThanksCellMemberRepository;
import com.gntcyouthbe.thanks.repository.ThanksCellRepository;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.transaction.support.TransactionTemplate;

// 내 구역을 정하는 요청은 잠그지 않으므로, 겹쳐도 오류 없이 한 행만 남는지 실제 DB로 확인한다.
@SpringBootTest
@ActiveProfiles("test")
class ThanksCellConcurrencyTest {

    private static final int REQUESTS = 8;
    private static final long USER_ID = 5L;

    @Autowired
    private ThanksCellService thanksCellService;

    @Autowired
    private ThanksCellRepository thanksCellRepository;

    @Autowired
    private ThanksCellMemberRepository thanksCellMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User user;
    private ThanksCell firstCell;
    private ThanksCell secondCell;

    @BeforeEach
    void setUp() {
        clear();
        user = userRepository.findById(USER_ID).orElseThrow();
        firstCell = thanksCellRepository.save(new ThanksCell("첫째구역", 1));
        secondCell = thanksCellRepository.save(new ThanksCell("둘째구역", 2));
    }

    @AfterEach
    void clear() {
        thanksCellMemberRepository.deleteAllInBatch();
        thanksCellRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("같은 사람이 서로 다른 구역을 동시에 여러 번 골라도 모두 성공하고 행은 하나만 남는다")
    void updateMyCellConcurrently() throws Exception {
        UserPrincipal principal = new UserPrincipal(user, null);
        ExecutorService executor = Executors.newFixedThreadPool(REQUESTS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();
        try {
            for (int i = 0; i < REQUESTS; i++) {
                Long cellId = (i % 2 == 0 ? firstCell : secondCell).getId();
                results.add(executor.submit(() -> {
                    start.await();
                    return thanksCellService.updateMyCell(principal, new ThanksMyCellUpdateRequest(cellId));
                }));
            }
            start.countDown();
            for (Future<?> result : results) {
                result.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(cellIdsOfUser()).hasSize(1).first().isIn(firstCell.getId(), secondCell.getId());
    }

    @Test
    @DisplayName("다른 요청이 넣는 중인 행 때문에 INSERT가 unique 제약에 걸려도, 새 트랜잭션에서 구역을 바꿔 성공하고 나중에 도착한 쪽이 이긴다")
    void updateMyCell_whileFirstInsertIsNotCommitted() throws Exception {
        UserPrincipal principal = new UserPrincipal(user, null);

        CompletableFuture<ThanksCellListResponse> laterRequest = transactionTemplate.execute(status -> {
            thanksCellMemberRepository.saveAndFlush(new ThanksCellMember(user, firstCell));
            CompletableFuture<ThanksCellListResponse> request = CompletableFuture.supplyAsync(() ->
                    thanksCellService.updateMyCell(principal, new ThanksMyCellUpdateRequest(secondCell.getId())));
            waitUntilRequestReachesInsert();
            return request;
        });

        assertThat(laterRequest.get(10, TimeUnit.SECONDS).myCellId()).isEqualTo(secondCell.getId());
        assertThat(cellIdsOfUser()).containsExactly(secondCell.getId());
    }

    // 뒤 요청은 아직 커밋되지 않은 행을 보지 못하고 INSERT까지 가서, 앞 트랜잭션이 끝나기를 기다린다.
    // H2가 기다려 주는 시간(기본 2초)보다 짧아야 한다.
    private void waitUntilRequestReachesInsert() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private List<Long> cellIdsOfUser() {
        return jdbcTemplate.queryForList("SELECT cell_id FROM thanks_cell_member WHERE user_id = ?", Long.class, USER_ID);
    }
}
