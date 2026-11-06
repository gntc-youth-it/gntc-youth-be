package com.gntcyouthbe.thanks.repository;

import com.gntcyouthbe.thanks.domain.ThanksCell;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThanksCellRepository extends JpaRepository<ThanksCell, Long> {

    List<ThanksCell> findAllByOrderBySortOrderAscIdAsc();
}
