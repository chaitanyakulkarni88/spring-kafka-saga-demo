package com.example.order.service;

import com.example.order.metrics.SagaMetrics;
import org.springframework.stereotype.Service;
import com.example.base.domain.Order;

@Service
public class OrderManageService {

    private final SagaMetrics sagaMetrics;

    public OrderManageService(SagaMetrics sagaMetrics) {
        this.sagaMetrics = sagaMetrics;
    }

    public Order confirm(Order orderPayment, Order orderStock) {
        return sagaMetrics.sagaTimer().record(() -> {
            Order o = new Order(orderPayment.getId(),
                    orderPayment.getCustomerId(),
                    orderPayment.getProductId(),
                    orderPayment.getProductCount(),
                    orderPayment.getPrice());
            if (orderPayment.getStatus().equals("ACCEPT") &&
                    orderStock.getStatus().equals("ACCEPT")) {
                o.setStatus("CONFIRMED");
                sagaMetrics.orderConfirmed();
            } else if (orderPayment.getStatus().equals("REJECT") &&
                    orderStock.getStatus().equals("REJECT")) {
                o.setStatus("REJECTED");
                sagaMetrics.orderRejected();
            } else if (orderPayment.getStatus().equals("REJECT") ||
                    orderStock.getStatus().equals("REJECT")) {
                String source = orderPayment.getStatus().equals("REJECT")
                        ? "PAYMENT" : "STOCK";
                o.setStatus("ROLLBACK");
                o.setSource(source);
                sagaMetrics.orderRollback();
            }
            return o;
        });
    }

}