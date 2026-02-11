package com.financetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.io.Serializable;

/**
 * Redis Configuration for caching and session management
 * Enables Redis-backed HTTP sessions for distributed session storage
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600) // 1 hour default session timeout
public class RedisConfig {

    /**
     * Redis Connection Factory using Lettuce
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    /**
     * Redis Template for general-purpose operations
     * Configures serialization for keys and values
     */
    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // JSON serializer for values
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // Configure key serialization
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Configure value serialization
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
