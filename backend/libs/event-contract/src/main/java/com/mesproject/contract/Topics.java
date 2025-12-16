package com.mesproject.contract;

public final class Topics {
    private Topics() {}

    public static final String RFID_RAW = "rfid.raw";
    public static final String RFID_NORMALIZED = "rfid.normalized";
    public static final String MES_DOMAIN_EVENTS = "mes.domain.events";
    public static final String ERP_INBOUND = "integration.erp.inbound";
    public static final String ERP_OUTBOX = "integration.erp.outbox";
    public static final String DLQ = "dlq";
}
