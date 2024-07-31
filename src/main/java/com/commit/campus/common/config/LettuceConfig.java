package com.commit.campus.common.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
public class LettuceConfig {

    @Value("${spring.data.redis.port}")
    public int port;

    @Value("${spring.data.redis.host}")
    public String host;

    @Bean
    public RedisClient redisClient() {
        // Redis 서버 URL 설정
        return RedisClient.create("redis://localhost:"+ port);
    }

    @Bean
    public StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
        // Redis 연결 생성
        return redisClient.connect();
    }

    @Bean
    public RedisAsyncCommands<String, String> redisAsyncCommands(StatefulRedisConnection<String, String> connection) {
        // 비동기 명령어 객체 생성
        return connection.async();      // 동기인 경우 connection.sync();
    }
}
