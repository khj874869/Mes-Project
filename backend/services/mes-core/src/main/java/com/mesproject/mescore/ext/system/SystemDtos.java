package com.mesproject.mescore.ext.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO 모음 (단순화를 위해 한 파일에 묶음).
 */
public class SystemDtos {

    public record LineView(String lineCode, String name, Boolean active) {}

    public record StationView(
            String stationCode,
            String name,
            String lineCode,
            Integer seq,
            Boolean kioskEnabled,
            Boolean active
    ) {}

    public static class UpsertLineRequest {
        @NotBlank
        private String lineCode;
        @NotBlank
        private String name;
        @NotNull
        private Boolean active = true;

        public String getLineCode() { return lineCode; }
        public void setLineCode(String lineCode) { this.lineCode = lineCode; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    public static class UpsertStationRequest {
        @NotBlank
        private String stationCode;
        @NotBlank
        private String name;
        @NotBlank
        private String lineCode;
        @NotNull
        private Integer seq;
        @NotNull
        private Boolean kioskEnabled = true;
        @NotNull
        private Boolean active = true;

        public String getStationCode() { return stationCode; }
        public void setStationCode(String stationCode) { this.stationCode = stationCode; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLineCode() { return lineCode; }
        public void setLineCode(String lineCode) { this.lineCode = lineCode; }
        public Integer getSeq() { return seq; }
        public void setSeq(Integer seq) { this.seq = seq; }
        public Boolean getKioskEnabled() { return kioskEnabled; }
        public void setKioskEnabled(Boolean kioskEnabled) { this.kioskEnabled = kioskEnabled; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }
}
