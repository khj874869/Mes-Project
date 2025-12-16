package com.mesproject.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JacksonSupport {
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private JacksonSupport(){}

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
