package com.zkrypto.zkmpc.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Value("${spring.rabbitmq.port}")
    private String port;
    @Value("${spring.rabbitmq.host}")
    private String host;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;

    public static final String TSS_EXCHANGE = "tss.exchange";

    public static final String TSS_ROUND_END_ROUTING_KEY_PREFIX = "topic.round.end";
    public static final String TSS_ROUND_COMPLETE_KEY_PREFIX = "topic.round.complete";
    public static final String TSS_ROUND_ROUTING_KEY_PREFIX = "topic.round";
    public static final String TSS_INIT_ROUTING_KEY_PREFIX = "topic.init";
    public static final String TSS_INIT_END_ROUTING_KEY_PREFIX = "topic.init.end";
    public static final String TSS_START_ROUTING_KEY_PREFIX = "topic.start";
    public static final String TSS_PROTOCOL_COMPLETE_KEY_PREFIX = "topic.complete";
    public static final String TSS_ERROR_HANDLE_KEY_PREFIX = "topic.error.handle";
    public static final String TSS_SIGN_KEY_PREFIX = "topic.sign";
    public static final String TSS_DLX_EXCHANGE = "tss.dead.letter.exchange";
    public static final String TSS_DLQ_ROUTING_KEY = "tss.dead.letter.key";

    @Bean
    public TopicExchange tssExchange() {
        return new TopicExchange(TSS_EXCHANGE);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setPort(Integer.parseInt(port));
        connectionFactory.setHost(host);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
