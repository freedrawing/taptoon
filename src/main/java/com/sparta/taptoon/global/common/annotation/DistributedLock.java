package com.sparta.taptoon.global.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String key(); // Lock Key
    TimeUnit timeUtil() default TimeUnit.SECONDS;
    long waitTime() default 5L; // Lock 획득 대기 시간
    long leaseTime() default 3L; // Lock 유지 시간
}
