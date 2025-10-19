package com.zkrypto.zkmpc.infrastructure.amqp;

import com.zkrypto.zkmpc.application.message.MessageBroker;
import com.zkrypto.zkmpc.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zkmpc.application.message.dto.ProtocolCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundEndEvent;
import com.zkrypto.zkmpc.common.config.RabbitMqConfig;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.InitProtocolEndMessage;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.ProceedRoundMessage;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.ProtocolCompleteMessage;
import com.zkrypto.zkmpc.infrastructure.amqp.dto.RoundCompleteMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqTssBroker implements MessageBroker {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(RoundEndEvent event) {
        String routingKey = RabbitMqConfig.TSS_ROUND_END_ROUTING_KEY_PREFIX;
        ProceedRoundMessage message = ProceedRoundMessage.builder().message(event.message()).type(event.type()).sid(event.sid()).build();
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(InitProtocolEndEvent event) {
        String routingKey = RabbitMqConfig.TSS_INIT_END_ROUTING_KEY_PREFIX;
        InitProtocolEndMessage message = InitProtocolEndMessage.builder()
                .sid(event.sid())
                .memberId(event.memberId())
                .type(event.type())
                .build();
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(ProtocolCompleteEvent event) {
        String routingKey = RabbitMqConfig.TSS_PROTOCOL_COMPLETE_KEY_PREFIX;
        ProtocolCompleteMessage message = ProtocolCompleteMessage.builder()
                .sid(event.sid())
                .memberId(event.memberId())
                .type(event.type())
                .build();
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(RoundCompleteEvent event) {
        String routingKey = RabbitMqConfig.TSS_ROUND_COMPLETE_KEY_PREFIX;
        RoundCompleteMessage message = RoundCompleteMessage.builder()
                .roundName(event.roundName())
                .sid(event.sid())
                .type(event.type())
                .build();
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }
}
