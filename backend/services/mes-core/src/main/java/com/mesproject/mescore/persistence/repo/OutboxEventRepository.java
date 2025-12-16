package com.mesproject.mescore.persistence.repo;

import com.mesproject.mescore.persistence.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop20ByStatusOrderByIdAsc(String status);
}
