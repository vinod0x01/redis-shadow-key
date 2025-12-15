package com.shadow.shadow.config;

import com.shadow.shadow.model.SessionData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean(name = "customStringRedisTemplate")
    public ReactiveRedisTemplate<String, String> stringRedisTemplate(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext(new StringRedisSerializer())
                .value(new StringRedisSerializer())
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    // 2. Bean for SessionData objects (used for actual chat data)
    @Bean(name = "sessionDataRedisTemplate")
    public ReactiveRedisTemplate<String, SessionData> sessionDataRedisTemplate(ReactiveRedisConnectionFactory factory) {

        // Use Jackson serializer for complex objects (SessionData)
        Jackson2JsonRedisSerializer<SessionData> serializer = new Jackson2JsonRedisSerializer<>(SessionData.class);

        RedisSerializationContext<String, SessionData> context = RedisSerializationContext
                .<String, SessionData>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
