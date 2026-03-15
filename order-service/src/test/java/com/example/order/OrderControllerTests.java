package com.example.order;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import com.example.base.domain.Order;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"orders"},
        partitions = 1,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@AutoConfigureTestRestTemplate
public class OrderControllerTests {

    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private KafkaTemplate<Long, Order> template;
    @Autowired
    private ConsumerFactory<Long, Order> factory;

    @Test
    void shouldCreateOrderAndPublishKafkaEvent() {
        Order o = new Order(1L, 1L, 1L, 10, 100);
        o = restTemplate.postForObject("/orders", o, Order.class);
        assertNotNull(o);
        assertEquals(1, o.getId());

        template.setConsumerFactory(factory);
        ConsumerRecord<Long, Order> rec = template.receive("orders", 0, 0, Duration.ofSeconds(5));
        assertNotNull(rec);
        assertNotNull(rec.value());
    }

    @Test
    void shouldPublishMultipleOrderEvents() {

        Order order1 = new Order(1L, 1L, 1L, 10, 100);
        Order order2 = new Order(2L, 2L, 2L, 5, 200);

        restTemplate.postForObject("/orders", order1, Order.class);
        restTemplate.postForObject("/orders", order2, Order.class);

        template.setConsumerFactory(factory);

        ConsumerRecord<Long, Order> rec1 =
                template.receive("orders", 0, 0, Duration.ofSeconds(5));

        ConsumerRecord<Long, Order> rec2 =
                template.receive("orders", 0, 1, Duration.ofSeconds(5));

        assertNotNull(rec1);
        assertNotNull(rec2);
    }
}
