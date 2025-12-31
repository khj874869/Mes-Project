package com.mesproject.mescore.service;

import com.mesproject.contract.JacksonSupport;
import com.mesproject.contract.RfidNormalizedEvent;
import com.mesproject.contract.WipMovedEvent;
import com.mesproject.mescore.persistence.*;
import com.mesproject.mescore.persistence.repo.*;
import com.mesproject.mescore.process.ProcessAutomationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WipService {

    private final ProcessedEventRepository processedRepo;
    private final WipUnitRepository unitRepo;
    private final WipEventRepository eventRepo;
    private final OutboxEventRepository outboxRepo;
    private final ProcessAutomationService automation;

    public WipService(ProcessedEventRepository processedRepo,
                      WipUnitRepository unitRepo,
                      WipEventRepository eventRepo,
                      OutboxEventRepository outboxRepo,
                      ProcessAutomationService automation) {
        this.processedRepo = processedRepo;
        this.unitRepo = unitRepo;
        this.eventRepo = eventRepo;
        this.outboxRepo = outboxRepo;
        this.automation = automation;
    }

    @Transactional
    public void handleNormalized(RfidNormalizedEvent e) {
        if (processedRepo.findByIdempotencyKey(e.idempotencyKey()).isPresent()) {
            return; // 이미 처리됨
        }
        processedRepo.save(new ProcessedEvent(e.idempotencyKey()));

        var unit = unitRepo.findByTagId(e.tagId())
                .orElseGet(() -> unitRepo.save(new WipUnit(e.tagId(), "SN-" + e.tagId())));

        String from = unit.getLastStation();
        unit.moveTo(e.stationCode());
        unitRepo.save(unit);

        eventRepo.save(new WipEvent(e.eventId(), e.idempotencyKey(), e.tagId(), e.stationCode(), e.direction(), e.occurredAt()));

        // 무인 공정 체크(OUT 시 SLA 초과/정체 등 자동 알람)
        automation.onScan(e.tagId(), e.stationCode(), e.direction(), e.occurredAt(), "RFID");

        var domainEvent = new WipMovedEvent(
                UUID.randomUUID().toString(),
                e.occurredAt(),
                e.idempotencyKey(),
                unit.getSerialNo(),
                from,
                e.stationCode()
        );

        try {
            String json = JacksonSupport.mapper().writeValueAsString(domainEvent);
            outboxRepo.save(new OutboxEvent("WIP_MOVED", unit.getSerialNo(), json));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
