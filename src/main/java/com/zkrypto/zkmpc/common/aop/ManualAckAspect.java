package com.zkrypto.zkmpc.common.aop;

import com.zkrypto.zkmpc.common.exception.ErrorCode;
import com.zkrypto.zkmpc.common.exception.TssException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.lang.annotation.Annotation;

@Slf4j
@Aspect
@Component
public class ManualAckAspect {

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
            log.warn("[AOP] 비즈니스 로직 실패. (Error: {})", t.getMessage());

            if (channel != null && deliveryTag != null) {
                log.error("[AOP] 비즈니스 로직 실패. NACK 전송. (Tag: {}, Error: {})", deliveryTag, t.getMessage());
                try {
                    channel.basicNack(deliveryTag, false, false);
                } catch (IOException e) {
                    throw new TssException(ErrorCode.RABBITMQ_CLIENT_ERROR);
                } finally {
                    log.error("[AOP] 비즈니스 로직 실패. NACK 전송. (Tag: {}, Error: {})", deliveryTag, t.getMessage());
                }
            } else {
                log.error("[AOP] 비즈니스 로직 실패, (Channel/Tag 없음) (Error: {})", t.getMessage());

            }

                return null;
        }
    }
}