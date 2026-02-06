package com.gntcyouthbe.advent.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.advent.domain.AdventPerson;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class AdventPersonRepositoryTest {

    @Autowired
    private AdventPersonRepository adventPersonRepository;

    @Test
    @DisplayName("이름, 성전, 기수로 어드벤트 사용자를 조회한다")
    void findByNameAndTempleAndBatch() {
        Optional<AdventPerson> person = adventPersonRepository.findByNameAndTempleAndBatch(
                "테스트", "Brisbane", 2024);

        assertThat(person).isPresent();
        assertThat(person.get().getName()).isEqualTo("테스트");
        assertThat(person.get().getVerses()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 조합은 빈 Optional을 반환한다")
    void findByNameAndTempleAndBatchNotFound() {
        Optional<AdventPerson> person = adventPersonRepository.findByNameAndTempleAndBatch(
                "없는사람", "Brisbane", 2024);

        assertThat(person).isEmpty();
    }
}
