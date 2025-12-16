package com.mesproject.integrationhub.consumer;

import com.mesproject.contract.Topics;
import com.mesproject.contract.WipMovedEvent;
import com.mesproject.integrationhub.persistence.InterfaceMessage;
import com.mesproject.integrationhub.persistence.repo.InterfaceMessageRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.mesproject.contract.JacksonSupport.mapper;

@Component
public class MesDomainEventConsumer {

    private final InterfaceMessageRepository repo;

    public MesDomainEventConsumer(InterfaceMessageRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(topics = Topics.MES_DOMAIN_EVENTS, groupId = "integration-hub")
    public void onMessage(WipMovedEvent e) throws Exception {
        // ERP로 나갈 메시지(outbox 느낌) 저장만 해두고,
        // 실제 ERP 전송은 이후 sender(배치/스케줄러)로 확장 가능.
        String idem = "mes:" + e.idempotencyKey();
        if (repo.findByIdempotencyKey(idem).isPresent()) return;

        repo.save(new InterfaceMessage(idem, "OUTBOUND", "ERP", "WIP_MOVED", mapper().writeValueAsString(e)));
    }
}
