package com.example.order.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class SagaMetrics {

    private final Counter ordersCreated;
    private final Counter ordersConfirmed;
    private final Counter ordersRejected;
    private final Counter ordersRollback;
    private final Timer sagaProcessingTimer;

    public SagaMetrics(MeterRegistry registry) {

        this.ordersCreated = Counter.builder("orders_created")
                .description("Total orders created")
                .register(registry);

        this.ordersConfirmed = Counter.builder("orders_confirmed")
                .description("Total orders confirmed")
                .register(registry);

        this.ordersRejected = Counter.builder("orders_rejected")
                .description("Total orders rejected")
                .register(registry);

        this.ordersRollback = Counter.builder("orders_rollback")
                .description("Total saga rollbacks")
                .register(registry);

        this.sagaProcessingTimer = Timer.builder("saga_processing_duration_seconds")
                .description("Time taken for saga processing")
                .register(registry);
    }

    public void orderCreated() {
        ordersCreated.increment();
    }

    public void orderConfirmed() {
        ordersConfirmed.increment();
    }

    public void orderRejected() {
        ordersRejected.increment();
    }

    public void orderRollback() {
        ordersRollback.increment();
    }

    public Timer sagaTimer() {
        return sagaProcessingTimer;
    }
}