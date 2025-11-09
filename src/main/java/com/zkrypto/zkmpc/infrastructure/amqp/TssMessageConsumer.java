package com.zkrypto.zkmpc.infrastructure.amqp;

import com.rabbitmq.client.Channel;
import com.zkrypto.dto.InitProtocolMessage;
import com.zkrypto.dto.ProceedRoundMessage;
import com.zkrypto.dto.StartProtocolMessage;
import com.zkrypto.zkmpc.application.tss.TssService;
import com.zkrypto.zkmpc.common.annotation.ManualAck;
import com.zkrypto.zkmpc.common.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TssMessageConsumer {

    private final TssService tssService;

    @ManualAck
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "tss.message.handle.${client.id}", durable = "true", exclusive = "false", autoDelete = "false",
                    arguments = {
                            @Argument(
                                    name = "x-dead-letter-exchange",
                                    value = RabbitMqConfig.TSS_DLX_EXCHANGE
                            ),
                            @Argument(
                                    name = "x-dead-letter-routing-key",
                                    value = RabbitMqConfig.TSS_DLQ_ROUTING_KEY
                            )
                    }),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_ROUND_ROUTING_KEY_PREFIX + "." + "${client.id}"
    ))
    public void handleTssMessage(ProceedRoundMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("라운드 메시지 수신");
        tssService.proceedRound(message.type(), message.message(), message.sid());
    }

    @ManualAck
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "tss.start.${client.id}", durable = "true", exclusive = "false", autoDelete = "false",
                    arguments = {
                            @Argument(
                                    name = "x-dead-letter-exchange",
                                    value = RabbitMqConfig.TSS_DLX_EXCHANGE
                            ),
                            @Argument(
                                    name = "x-dead-letter-routing-key",
                                    value = RabbitMqConfig.TSS_DLQ_ROUTING_KEY

                            )
                    }),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_START_ROUTING_KEY_PREFIX + "." + "${client.id}"
    ))
    public void startTssProtocol(StartProtocolMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("프로토콜 시작 메시지 수신");
        tssService.startProtocol(message);
    }

    @ManualAck
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "tss.init.${client.id}", durable = "true", exclusive = "false", autoDelete = "false",
                    arguments = {
                            @Argument(
                                    name = "x-dead-letter-exchange",
                                    value = RabbitMqConfig.TSS_DLX_EXCHANGE
                            ),
                            @Argument(
                                    name = "x-dead-letter-routing-key",
                                    value = RabbitMqConfig.TSS_DLQ_ROUTING_KEY

                            )
                    }),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_INIT_ROUTING_KEY_PREFIX + "." + "${client.id}"
    ))
    public void initTssProtocol(InitProtocolMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("프로토콜 초기화 메시지 수신");
        tssService.initProtocol(message);
    }
}
