# Microservices with Spring Boot, Kafka, Saga Transaction Demo Project

## Description
There are three microservices: \
`order-service` - it sends `Order` events to the Kafka topic and orchestrates the process of a distributed transaction \
`payment-service` - it performs local transaction on the customer account basing on the `Order` price \
`stock-service` - it performs local transaction on the store basing on number of products in the `Order`

## Architecture and Event Flow:
(1) `order-service` send a new `Order` -> `status == NEW` \
(2) `payment-service` and `stock-service` receive `Order` and handle it by performing a local transaction on the data \
(3) `payment-service` and `stock-service` send a reponse `Order` -> `status == ACCEPT` or `status == REJECT` \
(4) `order-service` process incoming stream of orders from `payment-service` and `stock-service`, join them by `Order` id and sends Order with a new status -> `status == CONFIRMATION` or `status == ROLLBACK` or `status == REJECTED` \
(5) `payment-service` and `stock-service` receive `Order` with a final status and "commit" or "rollback" a local transaction make before

Client
│
▼
Order Service
│
▼
orders topic (NEW)
│
├────────► Payment Service
│           │
│           ▼
│       payment-orders
│
└────────► Stock Service
│
▼
stock-orders

Kafka Streams Join
(payment + stock)

│
▼
orders topic (CONFIRMED / ROLLBACK)
│
▼
Payment + Stock finalize