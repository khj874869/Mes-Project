package com.mesproject.mescore.logging.controller;

import com.mesproject.mescore.logging.sse.SseHub;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/admin/logs")
public class LogStreamController {

    private final SseHub hub;

    public LogStreamController(SseHub hub) {
        this.hub = hub;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return hub.register();
    }
}
