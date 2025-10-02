package com.translation.domain.port.output;

import java.util.Optional;

public interface CacheRepository {
    Optional<String> get(String key);
    void put(String key, String value);
    void putWithTtl(String key, String value, long ttlSeconds);
    boolean exists(String key);
    void evict(String key);
}
