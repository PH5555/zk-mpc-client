package com.zkrypto.zkmpc.common.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.zkrypto.dto.ErrorMessage;
import com.zkrypto.zkmpc.common.config.RabbitMqConfig;
import com.zkrypto.zkmpc.common.exception.ErrorCode;
import com.zkrypto.zkmpc.common.exception.TssException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientMessageCleanRunner implements ApplicationRunner {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitListenerEndpointRegistry listenerRegistry;

    @Value("${client.id}")
    private String clientId;

    /**
     * 어플리케이션 시작 시, 남아있는 큐가 있으면 비정상 시작으로 간주하고
     * 오케스트레이션 서버로 재시작 요청을 보냅니다.
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> queueNameList = List.of(
                "tss.message.handle." + clientId,
                "tss.start." + clientId,
                "tss.init." + clientId);

        final AtomicReference<String> failedSid = new AtomicReference<>(null);

        Boolean restartNeeded = rabbitTemplate.execute(channel -> {
            boolean purgeOccurred = false;
            try {
                for (String queueName : queueNameList) {
                    // MPC 큐에 남아 있는 메시지 수 조회
                    long messageCount = channel.messageCount(queueName);

                    // 큐에 메시지가 남아 있으면 큐 비우기
                    if (messageCount > 0) {
                        // 큐의 메시지에서 sid 추출
                        failedSid.set(extractSid(channel, queueName));

                        log.info("{} 큐에 {}개의 메시지가 남아있어 비정상 재시작으로 간주합니다. 큐를 비웁니다.", queueName, messageCount);
                        channel.queuePurge(queueName);

                        // 큐를 비웠다는 사실을 기록
                        purgeOccurred = true;
                    }
                }

                return purgeOccurred;
            } catch (Exception e) {
                log.info(e.getMessage());
                throw new TssException(ErrorCode.RABBITMQ_QUEUE_RESET_ERROR);
            }
        });

        // 큐를 비운 경우에만 서버에 재시작 요청
        if (Boolean.TRUE.equals(restartNeeded)) {
            String sessionId = failedSid.get();
            ErrorMessage errorMessage = new ErrorMessage(sessionId, ErrorCode.RABBITMQ_RESTART.getMessage());

            rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_DLX_EXCHANGE, RabbitMqConfig.TSS_DLQ_ROUTING_KEY, errorMessage);
        }

        listenerRegistry.getListenerContainers().forEach(Lifecycle::start);
    }

    private String extractSid(Channel channel, String queueName) throws IOException {
        GetResponse response = channel.basicGet(queueName, false);

        if (response != null) {
            try {
                String body = new String(response.getBody(), StandardCharsets.UTF_8);
                JsonNode root = objectMapper.readTree(body);

                return root.path("sid").asText(null);
            } catch (Exception e) {
                log.warn("큐 메시지 본문 파싱 중 오류 발생 (sid 추출 실패): {}", e.getMessage());
            }
        }

        throw new TssException(ErrorCode.RABBITMQ_CHANNEL_ERROR);
    }
}
