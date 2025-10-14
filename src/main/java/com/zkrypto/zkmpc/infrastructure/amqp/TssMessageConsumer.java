package com.zkrypto.zkmpc.infrastructure.amqp;

import com.zkrypto.zkmpc.application.tss.TssService;
import com.zkrypto.zkmpc.common.config.RabbitMqConfig;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.InitProtocolMessage;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.ProceedRoundMessage;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.StartProtocolMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TssMessageConsumer {

    private final TssService tssService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "", durable = "true", exclusive = "true", autoDelete = "false"),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_ROUND_ROUTING_KEY_PREFIX + "${client.id}"
    ))
    public void handleTssMessage(ProceedRoundMessage message) {
        log.info("라운드 메시지 수신");
        tssService.proceedRound(message.type(), message.message(), message.sid());
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "", durable = "true", exclusive = "true", autoDelete = "false"),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_START_ROUTING_KEY_PREFIX + "${client.id}"
    ))
    public void startTssProtocol(StartProtocolMessage message) {
        log.info("프로토콜 시작 메시지 수신");
        tssService.startProtocol(message);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "", durable = "true", exclusive = "true", autoDelete = "false"),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_START_ROUTING_KEY_PREFIX + "${client.id}"
    ))
    public void startTssProtocol(InitProtocolMessage message) {
        log.info("프로토콜 초기화 메시지 수신");
        tssService.initProtocol(message);
    }
}
