package com.commit.campus.common.config;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.context.annotation.Configuration;
import io.lettuce.core.*;

@Configuration
public class RedisConfig {

    public void connectToRedis() {
        // RedisClient를 생성합니다.
        RedisClient redisClient = RedisClient.create("redis://localhost:6379");

        // StatefulRedisConnection을 생성합니다.
        StatefulRedisConnection<String, String> connection = redisClient.connect();

        // Redis 명령을 실행하기 위한 RedisCommands 인터페이스를 가져옵니다.
        RedisCommands<String, String> syncCommands = connection.sync();

        // 예제 명령어 실행
        syncCommands.set("key", "Hello, Lettuce!");
        String value = syncCommands.get("key");

        System.out.println("Stored value: " + value);

        // 연결을 닫습니다.
        connection.close();

        // RedisClient를 종료합니다.
        redisClient.shutdown();
    }
}
