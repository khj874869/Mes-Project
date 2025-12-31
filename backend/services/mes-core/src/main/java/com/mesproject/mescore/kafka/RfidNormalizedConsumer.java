package com.mesproject.mescore.kafka;

import com.mesproject.contract.RfidNormalizedEvent;
import com.mesproject.contract.Topics;
import com.mesproject.mescore.service.WipService;
import jakarta.persistence.Entity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RfidNormalizedConsumer {

    private final WipService wipService;

    public RfidNormalizedConsumer(WipService wipService) {
        this.wipService = wipService;
    }

    @KafkaListener(topics = Topics.RFID_NORMALIZED, groupId = "mes-core")
    public void onMessage(RfidNormalizedEvent e) {
        wipService.handleNormalized(e);
    }


}
