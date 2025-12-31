package com.mesproject.integrationhub.persistence.repo;

import com.mesproject.integrationhub.persistence.InterfaceMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterfaceMessageRepository extends JpaRepository<InterfaceMessage, Long> {
    Optional<InterfaceMessage> findByIdempotencyKey(String idempotencyKey);
}
