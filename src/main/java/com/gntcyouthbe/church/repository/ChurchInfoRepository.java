package com.gntcyouthbe.church.repository;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.domain.ChurchInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChurchInfoRepository extends JpaRepository<ChurchInfo, Long> {

    Optional<ChurchInfo> findByChurchId(ChurchId churchId);
}
