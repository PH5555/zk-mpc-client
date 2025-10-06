package com.zkrypto.zkmpc.infrastructure.amqp;

import com.zkrypto.zkmpc.application.tss.TssMessageBroker;
import com.zkrypto.zkmpc.common.config.RabbitMqConfig;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.ProceedRoundCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqTssBroker implements TssMessageBroker {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(String recipient, String message, String type) {
        String routingKey = RabbitMqConfig.TSS_DELIVER_ROUTING_KEY_PREFIX + recipient;
        ProceedRoundCommand command = ProceedRoundCommand.builder().message(message).type(type).build();
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, command);
    }
}
