package com.mesproject.integrationhub.erp.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WorkOrderUpsertRequest(
        @NotBlank String woNo,
        @NotBlank String itemCode,
        @NotNull Integer quantity,
        LocalDate dueDate
) {}
