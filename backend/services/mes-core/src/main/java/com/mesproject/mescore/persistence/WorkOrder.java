package com.mesproject.mescore.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name="work_order", uniqueConstraints = @UniqueConstraint(name="uk_wo_no", columnNames = "wo_no"))
public class WorkOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="wo_no", nullable=false, length=50)
    private String woNo;

    @Column(name="item_code", nullable=false, length=50)
    private String itemCode;

    @Column(name="quantity", nullable=false)
    private int quantity;

    @Column(name="due_date")
    private LocalDate dueDate;

    @Column(name="status", nullable=false, length=20)
    private String status = "OPEN";

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt = Instant.now();

    protected WorkOrder() {}

    public WorkOrder(String woNo, String itemCode, int quantity, LocalDate dueDate) {
        this.woNo = woNo;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.dueDate = dueDate;
    }

    public void update(String itemCode, int quantity, LocalDate dueDate) {
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.dueDate = dueDate;
        this.updatedAt = Instant.now();
    }


}
