package com.sehoaccountapi.config.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    public String host;

    @Value("${spring.data.redis.port}")
    public int port;

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        StringRedisSerializer string = new StringRedisSerializer();
        template.setKeySerializer(string);        // << prefix 붙이지 않음
        template.setHashKeySerializer(string);
        template.setValueSerializer(string);
        template.setHashValueSerializer(string);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager contentCacheManager(RedisConnectionFactory cf,
                                            @Value("account") String prefix) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 캐시 키 직렬화기: prefix 없는 일반 직렬화기
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofHours(3))
                // 여기서만 통일해서 붙임: account:<cacheName>:
                .computePrefixWith(cacheName -> prefix + ":" + cacheName + ":");

        return RedisCacheManager.builder(cf).cacheDefaults(config).build();
    }
}
