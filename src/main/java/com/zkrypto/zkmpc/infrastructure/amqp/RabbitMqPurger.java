package com.zkrypto.zkmpc.infrastructure.amqp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.zkrypto.zkmpc.application.message.MessagePurger;
import com.zkrypto.zkmpc.common.exception.ErrorCode;
import com.zkrypto.zkmpc.common.exception.TssException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RabbitMqPurger implements MessagePurger {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    @Value("${client.id}")
    private String clientId;

    private final List<String> queueNameList = List.of(
            "tss.message.handle.",
            "tss.start.",
            "tss.init.");

    @Override
    public Boolean purge() {
        return rabbitTemplate.execute(channel -> {
            boolean purgeOccurred = false;
            try {
                for (String queueName : queueNameList) {
                    queueName += clientId;
                    // MPC 큐에 남아 있는 메시지 수 조회
                    long messageCount = channel.messageCount(queueName);

                    // 큐에 메시지가 남아 있으면 큐 비우기
                    if (messageCount > 0) {

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
    }

    @Override
    public String purgeWithSid() {
        return rabbitTemplate.execute(channel -> {
            String sid = null;
            try {
                for (String queueName : queueNameList) {
                    queueName += clientId;
                    // MPC 큐에 남아 있는 메시지 수 조회
                    long messageCount = channel.messageCount(queueName);

                    // 큐에 메시지가 남아 있으면 큐 비우기
                    if (messageCount > 0) {

                        log.info("{} 큐에 {}개의 메시지가 남아있어 비정상 재시작으로 간주합니다. 큐를 비웁니다.", queueName, messageCount);
                        channel.queuePurge(queueName);
                        sid = extractSid(channel, queueName);
                    }
                }
                return sid;
            } catch (Exception e) {
                throw new TssException(ErrorCode.RABBITMQ_QUEUE_RESET_ERROR);
            }
        });
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
