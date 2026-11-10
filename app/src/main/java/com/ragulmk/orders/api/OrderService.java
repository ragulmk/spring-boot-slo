package com.ragulmk.orders.api;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();
    private final Counter ordersCreated;

    public OrderService(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("orders.created")
                .description("Number of orders created")
                .register(registry);
    }

    public Order create(String item, int quantity) {
        simulateWork();
        long id = ids.incrementAndGet();
        Order order = new Order(id, item, quantity, Instant.now());
        orders.put(id, order);
        ordersCreated.increment();
        return order;
    }

    public Optional<Order> find(long id) {
        simulateWork();
        return Optional.ofNullable(orders.get(id));
    }

    public Collection<Order> all() {
        simulateWork();
        return orders.values();
    }

    private void simulateWork() {
        sleep(ThreadLocalRandom.current().nextLong(10, 80));
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
