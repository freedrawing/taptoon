package com.sparta.taptoon.global.common.aop;

import com.sparta.taptoon.global.common.annotation.DistributedLock;
import com.sparta.taptoon.global.error.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "RedissonLock")
@Order(0)
@Aspect
@Component
@RequiredArgsConstructor
public class RedissonLockAspect {

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object executeWithLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String key = parseKey(distributedLock.key(), joinPoint);
        TimeUnit timeUnit = distributedLock.timeUtil();
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();

        RLock lock = redissonClient.getLock(key);

        try {
            // 락을 시도하는데, 최대 `waitTime`초(분, 시간)까지 대기하고, 락을 획득하면 `leaseTime`초(분, 시간) 후에는 자동 해제 되도록 설정
            boolean locked = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (locked == false) {
                log.error("Lock 획득 실패");
                throw new TooManyRequestsException();
            }

            log.info("Lock 획득");
            return joinPoint.proceed();

        } catch (Exception e) {
            throw new TooManyRequestsException();
        } finally {
            /** Tip)
             * 현재 스레드가 락을 획득한 상태인지 확인한 후 맞으면 unlock
             * Redisson의 unlock() 메서드는 락을 소유한 스레드만 해제할 수 있도록 제한되어 있음.
             * 만약 락을 소유하지 않은 상태에서 unlock()을 호출하면 예외가 발생.
             */
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock 해제");
            }
        }
    }

    // For SpringEL
    private String parseKey(String spELString, ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = methodSignature.getParameterNames();

        EvaluationContext context = new StandardEvaluationContext();

        // 파라미터 이름과 값을 SpEL context에 추가
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        String id = parser.parseExpression(spELString).getValue(context, String.class);
        return String.format("%s.%s:%s", className, methodName, id);
    }

}
