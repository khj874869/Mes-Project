package com.mesproject.mescore.telemetry.repo;

import com.mesproject.mescore.telemetry.domain.TelemetryReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, Long> {

    @Query(value = "select * from telemetry_reading " +
            "where site_id = :siteId " +
            "order by ts desc limit :limit",
            nativeQuery = true)
    List<TelemetryReading> findRecent(@Param("siteId") String siteId, @Param("limit") int limit);

    @Query(value = "select * from telemetry_reading " +
            "where site_id = :siteId and ts between :from and :to " +
            "order by ts asc",
            nativeQuery = true)
    List<TelemetryReading> findRange(@Param("siteId") String siteId, @Param("from") Instant from, @Param("to") Instant to);
}
