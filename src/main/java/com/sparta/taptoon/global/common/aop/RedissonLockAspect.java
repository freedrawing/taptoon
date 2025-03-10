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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "RedissonLock")
@Order(-1)
@Aspect
@Component
@RequiredArgsConstructor
public class RedissonLockAspect {

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();


    @Around("@annotation(distributedLock)")
    public Object executeWithLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String key = parseKey(distributedLock.key(), joinPoint);
        TimeUnit timeUnit = distributedLock.timeUnit();
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();

        RLock lock = redissonClient.getLock(key);

        try {
            // 락을 시도하는데, 최대 `waitTime`초(분, 시간)까지 대기하고, 락을 획득하면 `leaseTime`초(분, 시간) 후에는 자동 해제 되도록 설정
            boolean locked = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (locked == false) {
                log.error("❌ ({})Lock 획득 실패", key);
                throw new TooManyRequestsException();
            }

            log.info("({}) 🔒Lock 획득", key);
            return joinPoint.proceed();
        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
            throw new TooManyRequestsException();
        } catch (Exception e) {
            log.info("exception={}", e);
            throw e;
        } finally {
            /** Tip)
             * 현재 스레드가 락을 획득한 상태인지 확인한 후 맞으면 unlock
             * Redisson의 unlock() 메서드는 락을 소유한 스레드만 해제할 수 있도록 제한되어 있음.
             * 만약 락을 소유하지 않은 상태에서 unlock()을 호출하면 예외가 발생.
             */
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("({}) 🔓Lock 해제", key);
            }
        }
    }

    // 굳이 이렇게 사용하지 않아도 될 듯하다.
//    @Around("@annotation(distributedLock)")
//    public Object executeWithLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
//        String key = parseKey(distributedLock.key(), joinPoint);
//        TimeUnit timeUnit = distributedLock.timeUnit();
//        long waitTime = distributedLock.waitTime();
//        long leaseTime = distributedLock.leaseTime();
//
//        RLock lock = redissonClient.getLock(key);
//        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
//
//        boolean lockAcquired = false;
//        try {
//            lockAcquired = lock.tryLock(waitTime, leaseTime, timeUnit);
//            if (!lockAcquired) {
//                log.error("❌ ({}) Lock 획득 실패", key);
//                throw new TooManyRequestsException();
//            }
//
//            log.info("({}) 🔒 Lock 획득", key);
//
//            boolean isTransactionActiveBefore = TransactionSynchronizationManager.isActualTransactionActive();
//            log.info("({}) Before proceed - isTransactionActive: {}", key, isTransactionActiveBefore);
//
//            Object result = joinPoint.proceed();
//
//            boolean isTransactionActiveAfter = TransactionSynchronizationManager.isActualTransactionActive();
//            log.info("({}) After proceed - isTransactionActive: {}", key, isTransactionActiveAfter);
//
//            // 트랜잭션이 활성화된 상태에서 동기화 등록
//            if (method.isAnnotationPresent(Transactional.class) && isTransactionActiveAfter) {
//                log.info("({}) 트랜잭션 감지: 트랜잭션 완료 후 락 해제 예약", key);
//                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//                    @Override
//                    public void afterCompletion(int status) {
//                        if (lock.isHeldByCurrentThread()) {
//                            lock.unlock();
//                            log.info("({}) 🔓 Lock 해제 (트랜잭션 종료 후, status: {})",
//                                    key, status == STATUS_COMMITTED ? "COMMITTED" : "ROLLED_BACK");
//                        } else {
//                            log.warn("({}) 락이 현재 스레드에 의해 소유되지 않음", key);
//                        }
//                    }
//                });
//            }
//
//            return result;
//
//        } catch (InterruptedException e) {
//            throw new TooManyRequestsException();
//        } catch (Exception e) {
//            log.error("({}) 예외 발생: {}", key, e.getMessage(), e);
//            throw e;
//        } finally {
//            if (lockAcquired && (!method.isAnnotationPresent(Transactional.class) ||
//                    !TransactionSynchronizationManager.isActualTransactionActive())) {
//                if (lock.isHeldByCurrentThread()) {
//                    lock.unlock();
//                    log.info("({}) 🔓 Lock 해제 (트랜잭션 없음 또는 종료됨)", key);
//                } else {
//                    log.warn("({}) 락 해제 실패, 현재 스레드에 의해 소유되지 않음", key);
//                }
//            }
//        }
//    }

    private String parseKey(String spELString, ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = methodSignature.getParameterNames();

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        String id = parser.parseExpression(spELString).getValue(context, String.class);
        return String.format("%s.%s:%s", className, methodName, id);
    }
}