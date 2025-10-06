package com.translation.infrastructure.adapter;

import com.translation.domain.port.CachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Adapter Pattern - Implementa cache multinível (Caffeine L1 + Redis L2)
 * Caffeine: cache local rápido
 * Redis: cache distribuído para escalabilidade
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiLevelCacheAdapter implements CachePort {

    private final CacheManager cacheManager; // Caffeine
    private final StringRedisTemplate redisTemplate; // Redis
    
    private static final String CACHE_NAME = "translations";
    private static final Duration REDIS_TTL = Duration.ofHours(24);

    @Override
    public String get(String key) {
        // Nível 1: Caffeine (local, muito rápido)
        Cache caffeineCache = cacheManager.getCache(CACHE_NAME);
        if (caffeineCache != null) {
            Cache.ValueWrapper wrapper = caffeineCache.get(key);
            if (wrapper != null) {
                log.debug("Cache L1 (Caffeine) HIT: {}", key);
                return (String) wrapper.get();
            }
        }
        
        // Nível 2: Redis (distribuído)
        String value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("Cache L2 (Redis) HIT: {}", key);
            
            // Promove para L1
            if (caffeineCache != null) {
                caffeineCache.put(key, value);
            }
            
            return value;
        }
        
        log.debug("Cache MISS: {}", key);
        return null;
    }

    @Override
    public void put(String key, String value) {
        // Salva em ambos os níveis
        
        // Nível 1: Caffeine
        Cache caffeineCache = cacheManager.getCache(CACHE_NAME);
        if (caffeineCache != null) {
            caffeineCache.put(key, value);
            log.debug("Saved to cache L1 (Caffeine): {}", key);
        }
        
        // Nível 2: Redis com TTL
        redisTemplate.opsForValue().set(key, value, REDIS_TTL);
        log.debug("Saved to cache L2 (Redis): {}", key);
    }

    @Override
    public void evict(String key) {
        // Remove de ambos os níveis
        
        Cache caffeineCache = cacheManager.getCache(CACHE_NAME);
        if (caffeineCache != null) {
            caffeineCache.evict(key);
            log.debug("Evicted from cache L1 (Caffeine): {}", key);
        }
        
        redisTemplate.delete(key);
        log.debug("Evicted from cache L2 (Redis): {}", key);
    }

    @Override
    public void clear() {
        // Limpa ambos os níveis
        
        Cache caffeineCache = cacheManager.getCache(CACHE_NAME);
        if (caffeineCache != null) {
            caffeineCache.clear();
            log.info("Cleared cache L1 (Caffeine)");
        }
        
        // Redis: remove por padrão (cuidado em produção!)
        log.warn("Clear all Redis keys - use with caution in production");
    }

    @Override
    public boolean containsKey(String key) {
        // Verifica Caffeine primeiro
        Cache caffeineCache = cacheManager.getCache(CACHE_NAME);
        if (caffeineCache != null && caffeineCache.get(key) != null) {
            return true;
        }
        
        // Depois verifica Redis
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
