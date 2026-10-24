package com.scenic.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 缓存配置
 *
 * <p>监控看板类接口「读多写少 + 前端定时轮询」，用 Redis 做短 TTL 缓存削掉重复的聚合查询。
 * TTL 同时充当陈旧度上限：看板允许 30 秒内的数据陈旧，因此下单等高频写操作不主动清缓存，
 * 只有景点增删改（会改变热力图构成）才整体失效。
 *
 * <p>Redis 不可用时通过 {@link CacheErrorHandler} 降级为直接查库，缓存故障不影响主流程。
 */
@Slf4j
@Configuration
public class CacheConfig implements CachingConfigurer {

    /**
     * 看板数据允许的陈旧窗口
     */
    private static final Duration DASHBOARD_TTL = Duration.ofSeconds(30);

    private final RedisConnectionFactory connectionFactory;

    public CacheConfig(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DASHBOARD_TTL)
                .prefixCacheNameWith("scenic:")
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .build();
    }

    /**
     * 缓存读写异常只记录日志，不向上抛出：缓存是加速手段，不应成为可用性单点
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("缓存读取失败，降级为直接查库: cache={}, key={}, err={}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("缓存写入失败: cache={}, key={}, err={}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("缓存失效失败: cache={}, key={}, err={}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("缓存清空失败: cache={}, err={}",
                        cache.getName(), exception.getMessage());
            }
        };
    }
}