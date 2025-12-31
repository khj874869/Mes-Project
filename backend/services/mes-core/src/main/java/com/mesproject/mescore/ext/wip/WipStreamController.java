package com.mesproject.mescore.ext.wip;

import com.mesproject.mescore.logging.sse.SseHub;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/wip")
public class WipStreamController {

    private final SseHub hub;

    public WipStreamController(SseHub hub) {
        this.hub = hub;
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        return hub.register();
    }
}
