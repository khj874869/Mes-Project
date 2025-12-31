package com.mesproject.mescore.kafka;

import com.mesproject.contract.Topics;
import com.mesproject.contract.WipMovedEvent;
import com.mesproject.mescore.persistence.repo.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.mesproject.contract.JacksonSupport.mapper;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository outboxRepo, KafkaTemplate<String, Object> kafkaTemplate) {
        this.outboxRepo = outboxRepo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishBatch() {
        var batch = outboxRepo.findTop20ByStatusOrderByIdAsc("NEW");
        if (batch.isEmpty()) return;

        for (var row : batch) {
            try {
                if ("WIP_MOVED".equals(row.getEventType())) {
                    WipMovedEvent e = mapper().readValue(row.getPayloadJson(), WipMovedEvent.class);
                    kafkaTemplate.send(Topics.MES_DOMAIN_EVENTS, row.getAggregateId(), e);
                } else {
                    kafkaTemplate.send(Topics.DLQ, row.getAggregateId(), row.getPayloadJson());
                }
                row.markPublished();
                outboxRepo.save(row);
            } catch (Exception ex) {
                // 실패하면 다음 스케줄에서 재시도하도록 그대로 둠
            }
        }
    }

}
