package com.gntcyouthbe.christmas.repository;

import com.gntcyouthbe.christmas.domain.Ornament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrnamentRepository extends JpaRepository<Ornament, Long> {

    List<Ornament> findAllByOrderByCreatedAtDesc();
}
