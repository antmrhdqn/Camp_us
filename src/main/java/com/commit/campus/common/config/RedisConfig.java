package com.commit.campus.common.config;

import org.springframework.context.annotation.Bean;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.context.annotation.Configuration;
import io.lettuce.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.data.redis.port}")
    public int port;

    @Value("${spring.data.redis.host}")
    public String host;

    @Bean
    // spring data redis에서 redis와 통신하기 위해 등록
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();  // redis 연결 시 lettuce를 사용
    }

    @Bean
    // redis 데이터 작업을 위해 등록 key는 String, value는 Object 타입으로 설정
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}
