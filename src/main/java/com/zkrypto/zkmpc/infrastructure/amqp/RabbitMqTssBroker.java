package com.zkrypto.zkmpc.infrastructure.amqp;

import com.zkrypto.zkmpc.application.tss.TssMessageBroker;
import com.zkrypto.zkmpc.common.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqTssBroker implements TssMessageBroker {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(String recipient, String message) {
        String routingKey = RabbitMqConfig.TSS_DELIVER_ROUTING_KEY_PREFIX + recipient;
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }
}
