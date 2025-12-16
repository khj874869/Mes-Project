package com.mesproject.mescore.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="wip_unit", uniqueConstraints = @UniqueConstraint(name="uk_wip_tag", columnNames = "tag_id"))
public class WipUnit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="tag_id", nullable=false, length=100)
    private String tagId;

    @Column(name="serial_no", nullable=false, length=100)
    private String serialNo;

    @Column(name="last_station", length=50)
    private String lastStation;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt = Instant.now();

    protected WipUnit() {}

    public WipUnit(String tagId, String serialNo) {
        this.tagId = tagId;
        this.serialNo = serialNo;
    }

    public Long getId() { return id; }
    public String getTagId() { return tagId; }
    public String getSerialNo() { return serialNo; }
    public String getLastStation() { return lastStation; }

    public void moveTo(String station) {
        this.lastStation = station;
        this.updatedAt = Instant.now();
    }
}
