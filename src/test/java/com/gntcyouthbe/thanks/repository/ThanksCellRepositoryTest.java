package com.gntcyouthbe.thanks.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gntcyouthbe.thanks.domain.ThanksCell;
import com.gntcyouthbe.thanks.domain.ThanksCellMember;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ThanksCellRepositoryTest {

    @Autowired
    private ThanksCellRepository thanksCellRepository;

    @Autowired
    private ThanksCellMemberRepository thanksCellMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    @AfterEach
    void clear() {
        thanksCellMemberRepository.deleteAllInBatch();
        thanksCellRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("구역은 넣은 순서가 아니라 보여 줄 순서대로 나오고, 순서가 같으면 먼저 넣은 구역이 앞이다")
    void findAllByOrder() {
        thanksCellRepository.save(new ThanksCell("셋째구역", 3));
        thanksCellRepository.save(new ThanksCell("첫째구역", 1));
        thanksCellRepository.save(new ThanksCell("둘째구역", 2));
        thanksCellRepository.save(new ThanksCell("둘째와 같은 구역", 2));

        List<ThanksCell> cells = thanksCellRepository.findAllByOrderBySortOrderAscIdAsc();

        assertThat(cells).extracting(ThanksCell::getName)
                .containsExactly("첫째구역", "둘째구역", "둘째와 같은 구역", "셋째구역");
    }

    @Test
    @DisplayName("구역원을 찾으면 그 구역 사람만 나오고, 사용자가 함께 읽혀서 트랜잭션 밖에서도 이름을 볼 수 있다")
    void findByCellIdWithUser() {
        ThanksCell first = thanksCellRepository.save(new ThanksCell("첫째구역", 1));
        ThanksCell second = thanksCellRepository.save(new ThanksCell("둘째구역", 2));
        thanksCellMemberRepository.save(new ThanksCellMember(userRepository.findById(1L).orElseThrow(), first));
        thanksCellMemberRepository.save(new ThanksCellMember(userRepository.findById(2L).orElseThrow(), first));
        thanksCellMemberRepository.save(new ThanksCellMember(userRepository.findById(5L).orElseThrow(), second));

        List<ThanksCellMember> members = thanksCellMemberRepository.findByCellIdWithUser(first.getId());

        assertThat(members).extracting(member -> member.getUser().getId()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(members).extracting(member -> member.getUser().getName()).doesNotContainNull();
    }

    @Test
    @DisplayName("한 사람은 구역 행을 하나만 가질 수 있어서, 다른 구역이라도 또 저장하면 무결성 오류가 난다")
    void saveSecondCellOfSameUser() {
        User user = userRepository.findById(1L).orElseThrow();
        ThanksCell first = thanksCellRepository.save(new ThanksCell("첫째구역", 1));
        ThanksCell second = thanksCellRepository.save(new ThanksCell("둘째구역", 2));
        thanksCellMemberRepository.saveAndFlush(new ThanksCellMember(user, first));

        assertThatThrownBy(() -> thanksCellMemberRepository.saveAndFlush(new ThanksCellMember(user, second)))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(thanksCellMemberRepository.count()).isEqualTo(1);
    }
}
