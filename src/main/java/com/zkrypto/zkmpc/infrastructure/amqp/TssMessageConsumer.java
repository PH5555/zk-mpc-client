package com.zkrypto.zkmpc.infrastructure.amqp;

import com.zkrypto.zkmpc.application.tss.TssService;
import com.zkrypto.zkmpc.common.config.RabbitMqConfig;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.ProceedRoundCommand;
import com.zkrypto.zkmpc.infrastructure.web.tss.dto.InitProtocolCommand;
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
            key = RabbitMqConfig.TSS_DELIVER_ROUTING_KEY_PREFIX + "${client.id}"
    ))
    public void handleTssMessage(ProceedRoundCommand command) {
        log.info("라운드 메시지 수신");
        tssService.proceedRound(command.type(), command.message(), command.sid());
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "", durable = "true", exclusive = "true", autoDelete = "false"),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_INIT_ROUTING_KEY_PREFIX + "${client.id}"
    ))
    public void initTssMessage(InitProtocolCommand command) {
        log.info("프로토콜 시작 메시지 수신");
        tssService.initProtocol(command);
    }
}
