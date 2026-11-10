package com.ragulmk.orders.chaos;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class ChaosInterceptor implements HandlerInterceptor {

    private final ChaosSettings settings;

    public ChaosInterceptor(ChaosSettings settings) {
        this.settings = settings;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long latency = settings.latencyMs();
        if (latency > 0) {
            try {
                Thread.sleep(latency);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (settings.errorRate() > 0 && ThreadLocalRandom.current().nextDouble() < settings.errorRate()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Injected failure (chaos)");
        }
        return true;
    }
}
