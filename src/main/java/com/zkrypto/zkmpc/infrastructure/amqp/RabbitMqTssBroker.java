package com.zkrypto.zkmpc.infrastructure.amqp;

import com.zkrypto.dto.InitProtocolEndMessage;
import com.zkrypto.dto.ProceedRoundMessage;
import com.zkrypto.dto.ProtocolCompleteMessage;
import com.zkrypto.dto.RoundCompleteMessage;
import com.zkrypto.zkmpc.application.message.MessageBroker;
import com.zkrypto.zkmpc.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zkmpc.application.message.dto.ProtocolCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundCompleteEvent;
import com.zkrypto.zkmpc.application.message.dto.RoundEndEvent;
import com.zkrypto.zkmpc.common.config.RabbitMqConfig;
import com.zkrypto.zkmpc.infrastructure.amqp.mapper.MessageMapper;
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
        ProceedRoundMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(InitProtocolEndEvent event) {
        String routingKey = RabbitMqConfig.TSS_INIT_END_ROUTING_KEY_PREFIX;
        InitProtocolEndMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(ProtocolCompleteEvent event) {
        String routingKey = RabbitMqConfig.TSS_PROTOCOL_COMPLETE_KEY_PREFIX;
        ProtocolCompleteMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(RoundCompleteEvent event) {
        String routingKey = RabbitMqConfig.TSS_ROUND_COMPLETE_KEY_PREFIX;
        RoundCompleteMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(String sign, String sid) {
        String routingKey = RabbitMqConfig.TSS_SIGN_KEY_PREFIX;
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, sign + "/" + sid);
    }
}
