package com.example.order.service;

import com.example.order.metrics.SagaMetrics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.example.base.domain.Order;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderGeneratorService {

    private static Random RAND = new Random();
    private AtomicLong id = new AtomicLong();
    private Executor executor;
    private KafkaTemplate<Long, Order> template;
    private final SagaMetrics sagaMetrics;

    public OrderGeneratorService(Executor executor, KafkaTemplate<Long, Order> template, SagaMetrics sagaMetrics) {
        this.executor = executor;
        this.template = template;
        this.sagaMetrics = sagaMetrics;
    }

    @Async
    public void generate() {
        for (int i = 0; i < 4000; i++) {
            int x = RAND.nextInt(5) + 1;
            Order o = new Order(id.incrementAndGet(), RAND.nextLong(100) + 1, RAND.nextLong(100) + 1, "NEW");
            o.setPrice(100 * x);
            o.setProductCount(x);
            sagaMetrics.orderCreated();
            template.send("orders", o.getId(), o);
        }
    }
}
