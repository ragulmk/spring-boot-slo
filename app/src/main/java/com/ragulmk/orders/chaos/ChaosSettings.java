package com.ragulmk.orders.chaos;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ChaosSettings {

    private final AtomicReference<Double> errorRate = new AtomicReference<>(0.0);
    private final AtomicLong latencyMs = new AtomicLong();

    public ChaosSettings(@Value("${chaos.error-rate:0.0}") double initialErrorRate,
                         @Value("${chaos.latency-ms:0}") long initialLatencyMs,
                         MeterRegistry registry) {
        update(initialErrorRate, initialLatencyMs);
        Gauge.builder("chaos.error.rate", this, ChaosSettings::errorRate)
                .description("Injected error rate (0-1)").register(registry);
        Gauge.builder("chaos.latency", this, s -> s.latencyMs() / 1000.0)
                .description("Injected extra latency").baseUnit("seconds").register(registry);
    }

    public void update(double newErrorRate, long newLatencyMs) {
        if (newErrorRate < 0 || newErrorRate > 1) {
            throw new IllegalArgumentException("errorRate must be between 0 and 1");
        }
        if (newLatencyMs < 0 || newLatencyMs > 10_000) {
            throw new IllegalArgumentException("latencyMs must be between 0 and 10000");
        }
        errorRate.set(newErrorRate);
        latencyMs.set(newLatencyMs);
    }

    public double errorRate() {
        return errorRate.get();
    }

    public long latencyMs() {
        return latencyMs.get();
    }
}
