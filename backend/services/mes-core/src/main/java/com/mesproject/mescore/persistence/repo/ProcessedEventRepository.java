package com.mesproject.mescore.persistence.repo;

import com.mesproject.mescore.persistence.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    Optional<ProcessedEvent> findByIdempotencyKey(String idempotencyKey);
}
