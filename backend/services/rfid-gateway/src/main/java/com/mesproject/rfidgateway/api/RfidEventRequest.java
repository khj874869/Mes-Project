package com.mesproject.rfidgateway.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RfidEventRequest(
        @NotBlank String deviceId,
        @NotBlank String tagId,
        @NotBlank String stationCode,
        @NotBlank String direction,
        @NotNull Long seq
) {}
