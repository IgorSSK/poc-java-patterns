package com.translation.domain.port;

/**
 * Port (Hexagonal Architecture) - Interface para cache
 * Abstração para diferentes implementações de cache
 */
public interface CachePort {
    
    String get(String key);
    
    void put(String key, String value);
    
    void evict(String key);
    
    void clear();
    
    boolean containsKey(String key);
}
