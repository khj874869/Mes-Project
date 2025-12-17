package com.mesproject.mescore.kafka;

import com.mesproject.contract.ErpWorkOrderUpsertEvent;
import com.mesproject.contract.Topics;
import com.mesproject.mescore.persistence.WorkOrder;
import com.mesproject.mescore.persistence.repo.WorkOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ErpWorkOrderConsumer {

    private final WorkOrderRepository workOrderRepository;

    public ErpWorkOrderConsumer(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional
    @KafkaListener(topics = Topics.ERP_INBOUND, groupId = "mes-core-erp")
    public void onMessage(ErpWorkOrderUpsertEvent e) {
        workOrderRepository.findByWoNo(e.woNo())
                .ifPresentOrElse(
                        wo -> wo.update(e.itemCode(), e.quantity(), e.dueDate()),
                        () -> workOrderRepository.save(new WorkOrder(e.woNo(), e.itemCode(), e.quantity(), e.dueDate()))
                );
    }

}
