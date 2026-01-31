package com.gntcyouthbe.advent.repository;

import com.gntcyouthbe.advent.domain.AdventPerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdventPersonRepository extends JpaRepository<AdventPerson, Long> {

    Optional<AdventPerson> findByNameAndTempleAndBatch(String name, String temple, Integer batch);
}
