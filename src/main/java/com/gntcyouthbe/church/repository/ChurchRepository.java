package com.gntcyouthbe.church.repository;

import com.gntcyouthbe.church.domain.Church;
import com.gntcyouthbe.church.domain.ChurchId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChurchRepository extends JpaRepository<Church, ChurchId> {
}
