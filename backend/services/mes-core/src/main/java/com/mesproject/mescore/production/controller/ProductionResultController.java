package com.mesproject.mescore.production.controller;

import com.mesproject.mescore.production.domain.ProductionResult;
import com.mesproject.mescore.production.dto.ProductionResultEvent;
import com.mesproject.mescore.production.repo.ProductionResultRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static com.mesproject.contract.Topics.PRODUCTION_RESULT;

@RestController
public class ProductionResultController {

    private final ProductionResultRepository repo;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProductionResultController(ProductionResultRepository repo, KafkaTemplate<String, Object> kafkaTemplate) {
        this.repo = repo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/app/results")
    public List<ProductionResult> pageForUser(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Instant cursorCreatedAt,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        int safe = Math.min(Math.max(limit, 1), 200);
        return repo.fetchPage(from, to, cursorCreatedAt, cursorId, safe);
    }

    @GetMapping("/admin/results")
    public List<ProductionResult> pageForAdmin(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Instant cursorCreatedAt,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "100") int limit
    ) {
        int safe = Math.min(Math.max(limit, 1), 500);
        return repo.fetchPage(from, to, cursorCreatedAt, cursorId, safe);
    }

    @PostMapping("/admin/results/ingest")
    public void ingest(@RequestBody ProductionResultEvent e) {
        kafkaTemplate.send(PRODUCTION_RESULT, e.workOrderNo(), e);
    }
}
