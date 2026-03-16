# Microservices with Spring Boot, Kafka, Saga Transaction Demo

This project demonstrates a **Saga-based distributed transaction** using an event-driven microservices architecture.

It uses:

- Spring Boot
- Apache Kafka
- Kafka Streams
- Docker
- Saga Pattern (Orchestration)

The goal of this demo is to show how **distributed transactions across microservices** can be implemented using **Kafka events and stream processing**.

---

## Description

The system consists of **three microservices**.

### order-service
- Creates new orders
- Publishes `Order` events to Kafka
- Acts as the **Saga orchestrator**
- Uses Kafka Streams to join responses from other services and determine the final order status

### payment-service
- Performs a local transaction on the **customer account**
- Verifies if the customer has enough balance
- Sends response events (`ACCEPT` or `REJECT`)

### stock-service
- Performs a local transaction on **product inventory**
- Verifies if sufficient stock is available
- Sends response events (`ACCEPT` or `REJECT`)

## Architecture and Event Flow:
(1) `order-service` send a new `Order` -> `status == NEW`  \
(2) `payment-service` and `stock-service` receive `Order` and handle it by performing a local transaction on the data  \
(3) `payment-service` and `stock-service` send a response `Order` -> `status == ACCEPT` or `status == REJECT`  \
(4) `order-service` process incoming stream of orders from `payment-service` and `stock-service`, join them by `Order` id and sends Order with a new status -> `status == CONFIRMATION` or `status == ROLLBACK` or `status == REJECTED`  \
(5) `payment-service` and `stock-service` receive `Order` with a final status and "commit" or "rollback" a local transaction make before  \

![img_1.png](img_1.png)

## Running the Project with Docker

There are **two ways to run the project**.

---

## Option 1 — Run Entire System in Docker

This runs **Kafka and all microservices inside containers**.

### Step 1 — Build Docker Images

From the project root: **mvn clean package -DskipTests -Pbuild-image**

This builds Docker images for: order-service, payment-service, stock-service

###  Step 2 — Start All Services

From the project root: **docker compose up -d**

This starts: Kafka broker, order-service, payment-service, stock-service

###  Step 3 — Verify Containers

**docker ps**

Expected containers: broker, order-service, payment-service, stock-service

### Step 4 — Test the System

Create a new order:

**curl -X POST http://localhost:8080/orders \
-H "Content-Type: application/json" \
-d '{"customerId":1,"productId":1,"productCount":2,"price":200,"status":"NEW"}'**

Create 10000 orders:

**curl -X POST http://localhost:8080/orders/generate**

Retrieve all orders:

**GET http://localhost:8080/orders**

Expected result:

**status = CONFIRMED**

## Option 2 — Hybrid Mode (Run One Service Locally)

This mode is useful when debugging a specific service. Example: debugging order-service.

### Step 1 — Start Dependencies

Navigate to the service directory: order-service

Run: **docker compose up -d**

This starts: Kafka, payment-service, stock-service

### Step 2 — Run the Service Locally

Start the service from your IDE or terminal: **mvn spring-boot:run**

Now the architecture becomes:

Kafka (Docker)
payment-service (Docker)
stock-service (Docker)
order-service (Local JVM)

This allows you to debug with breakpoints.

## Observability
This project integrates Prometheus + Grafana to monitor the Saga workflow and system performance.

Prometheus collects application metrics exposed by Spring Boot Actuator, while Grafana visualizes them through dashboards.

### Grafana dashboard
**Saga Success Rate**

Shows the percentage of orders successfully completed through the entire saga workflow.

**orders_confirmed_total / orders_total * 100**

**Saga Failure Rate**

Represents the percentage of orders that failed due to payment or stock validation failures.

**(orders_rejected_total + orders_rollback_total) / orders_total * 100**

![img_3.png](img_3.png)

**Saga Processing Timer**

Measures the time taken for saga workflows to complete using the custom metric:

**saga_processing_duration_seconds**

This helps identify slow processing or performance bottlenecks in distributed services.

**Orders In Flight**

Displays the number of orders currently being processed in the saga pipeline.

**orders_total -
(orders_confirmed_total + orders_rejected_total + orders_rollback_total)**

A high value may indicate delays in downstream services.

![img_2.png](img_2.png)

### Prometheus metrics
**System metrics**

**http_server_requests_seconds_sum**    -   Total HTTP request processing time
**jvm_memory_used_bytes**   -   JVM memory usage
**jvm_threads_live_threads**    -	Number of active JVM threads
![img_4.png](img_4.png)
![img_5.png](img_5.png)
**Custom Saga metrics**

**orders_total**    Total orders generated
**orders_confirmed_total**  Orders successfully processed
**orders_rejected_total**   Orders rejected due to business rules
**orders_rollback_total**   Orders rolled back due to failures

![img_6.png](img_6.png)