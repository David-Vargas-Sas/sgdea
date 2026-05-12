package com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sgdea.multitenancy.multitenancy.securityConfig.application.dto.JwtSessionCacheDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuración de Redis para el caché de sesiones JWT.
 * Utiliza serialización JSON con soporte para tipos de Java 8 (LocalDateTime).
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, JwtSessionCacheDto> jwtSessionRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        ObjectMapper redisObjectMapper = new ObjectMapper();
        redisObjectMapper.registerModule(new JavaTimeModule());
        redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<JwtSessionCacheDto> serializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper, JwtSessionCacheDto.class);

        RedisTemplate<String, JwtSessionCacheDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}

