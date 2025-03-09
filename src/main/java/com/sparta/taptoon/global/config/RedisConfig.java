package com.sparta.taptoon.global.config;

import com.sparta.taptoon.global.redis.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    /**
     * Redis 서버와 연결을 설정하는 역할
     * LettuceConnectionFactory 를 사용하여 Redis 서버와 연결
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    /**
     * Redis 메시지 리스너 컨테이너
     *
     * Redis Pub/Sub에서 메시지를 수신하는 컨테이너
     * connectionFactory를 이용하여 Redis와의 연결을 유지하면서 메시지를 수신
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(messageListenerAdapter(null), new ChannelTopic("chatroom-*")); // 채널 패턴 추가
        log.info("✅ MessageListenerContainer 생성 완료! Active: {}", container.isRunning());
        return container;
    }

    /**
     *Redis 메시지 리스너 어댑터
     *
     * Redis로부터 메시지를 수신했을 때 호출될 메서드를 설정하는 역할
     * redisSubscriber가 Redis에서 전달받은 메시지를 처리하도록 onMessage 메서드에 바인딩
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisSubscriber redisSubscriber) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(redisSubscriber, "onMessage");
        log.info("✅ MessageListenerAdapter 생성 완료! Delegate: {}", redisSubscriber.getClass().getName());
        return adapter;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}




