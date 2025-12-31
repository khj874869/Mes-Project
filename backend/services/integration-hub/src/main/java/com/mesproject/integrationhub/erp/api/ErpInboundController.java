package com.mesproject.integrationhub.erp.api;

import com.mesproject.contract.ErpWorkOrderUpsertEvent;
import com.mesproject.contract.Topics;
import com.mesproject.integrationhub.persistence.InterfaceMessage;
import com.mesproject.integrationhub.persistence.repo.InterfaceMessageRepository;
import jakarta.validation.Valid;
import org.hibernate.engine.query.internal.NativeQueryInterpreterStandardImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

import static com.mesproject.contract.JacksonSupport.mapper;

@RestController
@RequestMapping("/erp")
public class ErpInboundController {

    private final InterfaceMessageRepository repo;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ErpInboundController(InterfaceMessageRepository repo, KafkaTemplate<String, Object> kafkaTemplate) {
        this.repo = repo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/work-orders")
    public ResponseEntity<?> upsert(@RequestHeader(value="X-Request-Id", required=false) String requestId,
                                   @Valid @RequestBody WorkOrderUpsertRequest req) throws Exception {

        String idempotencyKey = StringUtils.hasText(requestId)
                ? "erp:" + requestId
                : "erp:" + req.woNo() + ":" + req.itemCode();

        // 멱등 처리: 이미 같은 키로 들어온 요청이면 저장/발행 스킵
        if (repo.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return ResponseEntity.ok(java.util.Map.of("status","DUPLICATE","idempotencyKey",idempotencyKey));
        }

        String eventId = UUID.randomUUID().toString();
        var event = new ErpWorkOrderUpsertEvent(
                eventId,
                Instant.now(),
                idempotencyKey,
                req.woNo(),
                req.itemCode(),
                req.quantity(),
                req.dueDate()
        );
        repo.save(new InterfaceMessage(idempotencyKey, "INBOUND", "ERP", "WORK_ORDER_UPSERT", mapper().writeValueAsString(req)));
        kafkaTemplate.send(Topics.ERP_INBOUND, req.woNo(), event);

        return ResponseEntity.accepted().body(java.util.Map.of(
                "status","ACCEPTED",
                "eventId", eventId,
                "idempotencyKey", idempotencyKey
        ));
    }
}
