package com.zkrypto.zkmpc.common.aop;

import com.zkrypto.dto.ErrorMessage;
import com.zkrypto.zkmpc.common.config.RabbitMqConfig;
import com.zkrypto.zkmpc.common.exception.ErrorCode;
import com.zkrypto.zkmpc.common.exception.TssException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ManualAckAspect {
    private final RabbitTemplate rabbitTemplate;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new StandardReflectionParameterNameDiscoverer();
    private final String MESSAGE_DTO = "message";
    private final String SESSION_ID = "sid";

    @Around("@annotation(com.zkrypto.zkmpc.common.annotation.ManualAck)")
    public Object handleManualAck(ProceedingJoinPoint pjp) {
        Channel channel = null;
        Long deliveryTag = null;

        try {
            // 리스너 메소드의 파라미터에서 Channel과 deliveryTag 찾기
            Object[] args = pjp.getArgs();
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Annotation[][] paramAnnotations = signature.getMethod().getParameterAnnotations();

            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Channel) {
                    channel = (Channel) args[i];
                }

                for (Annotation annotation : paramAnnotations[i]) {
                    if (annotation instanceof Header &&
                            AmqpHeaders.DELIVERY_TAG.equals(((Header) annotation).value())) {
                        deliveryTag = (Long) args[i];
                    }
                }
            }

            if (channel == null || deliveryTag == null) {
                throw new TssException(ErrorCode.NOT_RABBITMQ_TARGET_ERROR);
            }

            // 비즈니스 로직 실행
            Object result = pjp.proceed();

            // ACK 전송
            log.info("[AOP] 비즈니스 성공. ACK 전송. (Tag: {})", deliveryTag);
            channel.basicAck(deliveryTag, false);
            return result;
        } catch (Throwable t) {
            // NACK 대신, sessionId를 DLQ로 직접 전송
            log.warn("[AOP] 비즈니스 로직 실패. (Error: {})", t.getMessage());

            try {
                if (channel == null || deliveryTag == null) {
                    throw new TssException(ErrorCode.NOT_RABBITMQ_TARGET_ERROR);
                }

                String sessionId = findSessionIdValue(pjp);
                ErrorMessage errorMessage = new ErrorMessage(sessionId, t.getMessage());

                // DLX로 메시지 직접 전송
                rabbitTemplate.convertAndSend(
                        RabbitMqConfig.TSS_DLX_EXCHANGE,
                        RabbitMqConfig.TSS_DLQ_ROUTING_KEY,
                        errorMessage
                );
                log.info("[AOP] {} 세션 에러, DLX로 전송 완료.", sessionId);

                channel.basicAck(deliveryTag, false);

            } catch (Exception e) {
                log.error("[AOP] DLQ 전송 및 ACK 처리 중 오류 발생.", e);
            }

            return null;
        }
    }

    private String findSessionIdValue(ProceedingJoinPoint joinPoint) throws IllegalStateException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = parameterNameDiscoverer.getParameterNames(signature.getMethod());
        Object[] args = joinPoint.getArgs();

        if (paramNames == null) {
            throw new TssException(ErrorCode.RABBITMQ_LISTENER_PARAMETER_ERROR);
        }

        for (int i = 0; i < paramNames.length; i++) {
            if (MESSAGE_DTO.equals(paramNames[i])) {
                Object dto = args[i];
                if (dto == null) {
                    throw new TssException(ErrorCode.RABBITMQ_LISTENER_PARAMETER_ERROR);
                }

                try {
                    Method method = dto.getClass().getMethod(SESSION_ID);
                    Object value = method.invoke(dto);
                    return value.toString();
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new TssException(ErrorCode.RABBITMQ_LISTENER_PARAMETER_ERROR);
                }
            }
        }

        throw new TssException(ErrorCode.RABBITMQ_LISTENER_PARAMETER_ERROR);
    }
}