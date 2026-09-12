package com.smart.smartcart.config;

import com.smart.smartcart.model.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Product> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Product> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        // Key → String
        template.setKeySerializer(
                new StringRedisSerializer()
        );

        // Value → JSON
        template.setValueSerializer(
                new JacksonJsonRedisSerializer<>(Product.class)
        );

        template.afterPropertiesSet();

        return template;
    }
}