package com.zkrypto.zkmpc.common.aop;

import com.zkrypto.zkmpc.application.message.MessagePurger;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Aspect
@Component
@RequiredArgsConstructor
public class SidLockAspect {
    private final ParameterNameDiscoverer parameterNameDiscoverer = new StandardReflectionParameterNameDiscoverer();
    private final ConcurrentHashMap<String, Lock> lockMap = new ConcurrentHashMap<>();
    private final RabbitListenerEndpointRegistry listenerRegistry;
    private final MessagePurger messagePurger;

    private final String INIT_METHOD = "initTssProtocol";
    private final String MESSAGE_DTO = "errorMessage";
    private final String SESSION_ID = "sid";

    @Around("@annotation(com.zkrypto.zkmpc.common.annotation.SidLock)")
    public Object handleSidLock(ProceedingJoinPoint pjp) {
        Signature signature = pjp.getSignature();
        String sid = (String) findParameter(pjp, SESSION_ID);

        Lock lock = lockMap.computeIfAbsent(sid, k -> new ReentrantLock());
        lock.lock();

        try {
            if(signature.getName().equals(INIT_METHOD)) {
                listenerRegistry.stop();
                messagePurger.purge();
                listenerRegistry.start();
            }
            return pjp.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private Object findParameter(ProceedingJoinPoint joinPoint, String parameterName) throws IllegalStateException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = parameterNameDiscoverer.getParameterNames(signature.getMethod());
        Object[] args = joinPoint.getArgs();

        if (paramNames == null) {
            throw new IllegalStateException("메서드의 파라미터가 없습니다." + signature.getName());
        }

        for (int i = 0; i < paramNames.length; i++) {
            if (MESSAGE_DTO.equals(paramNames[i])) {
                Object dto = args[i];
                if (dto == null) {
                    throw new IllegalStateException(MESSAGE_DTO + "파라미터의 값이 없습니다.");
                }

                try {
                    Method method = dto.getClass().getMethod(parameterName);
                    return method.invoke(dto);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException(MESSAGE_DTO + "파라미터의 메서드 실행 오류");
                }
            }
        }

        throw new IllegalStateException(MESSAGE_DTO + "파라미터가 없습니다.");
    }
}
