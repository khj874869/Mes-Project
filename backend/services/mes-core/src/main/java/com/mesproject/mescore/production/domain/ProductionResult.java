package com.mesproject.mescore.production.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "production_result",
        indexes = {
                @Index(name="idx_prod_work_order", columnList="work_order_no"),
                @Index(name="idx_prod_line_created", columnList="line_code, created_at")
        }
)
public class ProductionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="work_order_no", nullable=false, length=60)
    private String workOrderNo;

    @Column(name="line_code", nullable=false, length=40)
    private String lineCode;

    @Column(name="station_code", length=40)
    private String stationCode;

    @Column(name="item_code", nullable=false, length=60)
    private String itemCode;

    @Column(name="qty_good", nullable=false)
    private int qtyGood;

    @Column(name="qty_ng", nullable=false)
    private int qtyNg;

    @Column(name="started_at")
    private Instant startedAt;

    @Column(name="ended_at")
    private Instant endedAt;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    protected ProductionResult() {}

    public ProductionResult(String workOrderNo, String lineCode, String stationCode, String itemCode,
                            int qtyGood, int qtyNg, Instant startedAt, Instant endedAt, Instant createdAt) {
        this.workOrderNo = workOrderNo;
        this.lineCode = lineCode;
        this.stationCode = stationCode;
        this.itemCode = itemCode;
        this.qtyGood = qtyGood;
        this.qtyNg = qtyNg;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public Long getId() { return id; }
    public String getWorkOrderNo() { return workOrderNo; }
    public String getLineCode() { return lineCode; }
    public String getStationCode() { return stationCode; }
    public String getItemCode() { return itemCode; }
    public int getQtyGood() { return qtyGood; }
    public int getQtyNg() { return qtyNg; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
