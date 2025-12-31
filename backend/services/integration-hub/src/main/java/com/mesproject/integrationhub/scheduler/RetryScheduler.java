package com.mesproject.integrationhub.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 실무에서는 ERP 전송 실패분 재시도 / Dead-letter 재처리 같은 로직이 여기에 들어감.
 */
@Component
public class RetryScheduler {

    @Scheduled(fixedDelay = 10_000)
    public void tick() {
        // TODO: implement retry
    }
}
