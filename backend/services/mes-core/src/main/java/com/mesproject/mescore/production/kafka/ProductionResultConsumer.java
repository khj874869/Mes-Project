package com.mesproject.mescore.production.kafka;

import com.mesproject.contract.Topics;
import com.mesproject.mescore.production.dto.ProductionResultEvent;
import com.mesproject.mescore.production.domain.ProductionResult;
import com.mesproject.mescore.production.repo.ProductionResultRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductionResultConsumer {

    private final ProductionResultRepository repo;

    public ProductionResultConsumer(ProductionResultRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(topics = Topics.PRODUCTION_RESULT, groupId = "mes-core-production")
    public void onMessage(ProductionResultEvent e) {
        repo.save(new ProductionResult(
                e.workOrderNo(),
                e.lineCode(),
                e.stationCode(),
                e.itemCode(),
                e.qtyGood(),
                e.qtyNg(),
                e.startedAt(),
                e.endedAt(),
                e.createdAt()
        ));
    }
}
