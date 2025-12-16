package com.mesproject.mescore.persistence.repo;

import com.mesproject.mescore.persistence.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    Optional<WorkOrder> findByWoNo(String woNo);
}
