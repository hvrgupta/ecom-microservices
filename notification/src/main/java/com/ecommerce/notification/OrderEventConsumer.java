package com.ecommerce.notification;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import payload.OrderCreatedEventDTO;

import java.util.Map;

@Service
public class OrderEventConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleOrderEvent(OrderCreatedEventDTO orderEvent) {
        System.out.println("Order Event Received: " + orderEvent);
    }
}
