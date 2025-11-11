package com.zkrypto.zkmpc.common.runner;

import com.zkrypto.dto.ErrorMessage;
import com.zkrypto.zkmpc.common.config.RabbitMqConfig;
import com.zkrypto.zkmpc.common.exception.ErrorCode;
import com.zkrypto.zkmpc.infrastructure.amqp.RabbitMqPurger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientMessageCleanRunner implements ApplicationRunner {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqPurger rabbitMqPurger;
    private final RabbitListenerEndpointRegistry listenerRegistry;

    /**
     * 어플리케이션 시작 시, 남아있는 큐가 있으면 비정상 시작으로 간주하고
     * 오케스트레이션 서버로 재시작 요청을 보냅니다.
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("client message clean runner start");
        String sid = rabbitMqPurger.purgeWithSid();
        listenerRegistry.getListenerContainers().forEach(Lifecycle::start);

        // 큐를 비운 경우에만 서버에 재시작 요청
        if (sid != null) {
            log.info("프로토콜 재시작 요청");
            ErrorMessage errorMessage = new ErrorMessage(sid, ErrorCode.RABBITMQ_RESTART.getMessage());

            rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, RabbitMqConfig.TSS_ERROR_HANDLE_KEY_PREFIX, errorMessage);
        }
    }
}
