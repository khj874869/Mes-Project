package com.mesproject.mescore.production.repo;

import com.mesproject.mescore.production.domain.ProductionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ProductionResultRepository extends JpaRepository<ProductionResult, Long> {

    @Query(value = "select * from production_result " +
            "where (:from is null or created_at >= :from) " +
            "  and (:to is null or created_at <= :to) " +
            "  and (:cursorCreatedAt is null or (created_at, id) < (:cursorCreatedAt, :cursorId)) " +
            "order by created_at desc, id desc " +
            "limit :limit",
            nativeQuery = true)
    List<ProductionResult> fetchPage(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit
    );
}
