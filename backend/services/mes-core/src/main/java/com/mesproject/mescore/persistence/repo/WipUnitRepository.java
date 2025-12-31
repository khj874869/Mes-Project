package com.mesproject.mescore.persistence.repo;

import com.mesproject.mescore.persistence.WipUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WipUnitRepository extends JpaRepository<WipUnit, Long> {
    Optional<WipUnit> findByTagId(String tagId);
}
