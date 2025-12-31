package com.mesproject.mescore.telemetry.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "telemetry_reading",
        indexes = {
                @Index(name="idx_telemetry_site_ts", columnList="site_id, ts"),
                @Index(name="idx_telemetry_site_metric_ts", columnList="site_id, metric, ts")
        }
)
public class TelemetryReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="site_id", nullable=false, length=50)
    private String siteId;

    @Column(name="sensor_id", nullable=false, length=80)
    private String sensorId;

    @Column(nullable=false, length=20)
    private String metric;

    @Column(nullable=false)
    private double value;

    @Column(nullable=false)
    private Instant ts;

    protected TelemetryReading() {}

    public TelemetryReading(String siteId, String sensorId, String metric, double value, Instant ts) {
        this.siteId = siteId;
        this.sensorId = sensorId;
        this.metric = metric;
        this.value = value;
        this.ts = ts;
    }

    public Long getId() { return id; }
    public String getSiteId() { return siteId; }
    public String getSensorId() { return sensorId; }
    public String getMetric() { return metric; }
    public double getValue() { return value; }
    public Instant getTs() { return ts; }
}
