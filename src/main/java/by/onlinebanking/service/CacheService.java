package by.onlinebanking.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class CacheService {
    private static final Logger LOGGER = Logger.getLogger(CacheService.class.getName());

    private static final int MAX_CACHE_SIZE = 100;

    private final Map<String, CacheEntry> cache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            boolean shouldRemove = size() > MAX_CACHE_SIZE;
            if (shouldRemove) {
                LOGGER.log(Level.INFO, "Evicting oldest entry: {0} ", eldest.getKey());
            }
            return shouldRemove;
        }
    };
    private static final long TTL = TimeUnit.MINUTES.toMillis(15);

    private static class CacheEntry {
        @Getter
        private final Object value;
        private final long createdAt;

        public CacheEntry(Object value) {
            this.value = value;
            this.createdAt = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > TTL;
        }
    }

    public void enforceSizeLimit() {
        while (cache.size() > MAX_CACHE_SIZE) {
            String eldestKey = cache.keySet().iterator().next();
            cache.remove(eldestKey);
            LOGGER.log(Level.INFO, "[CACHE] Removed eldest entry due to size limit: {0} ", eldestKey);
        }
    }

    public void put(String key, Object value) {
        cache.put(key, new CacheEntry(value));
        enforceSizeLimit();
        LOGGER.log(Level.INFO, "[CACHE] Data added to cache with key: {0} ", key);
    }

    public Optional<Object> get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            LOGGER.log(Level.INFO, "[CACHE] Data not found in cache for key: {0} ", key);
            return Optional.empty();
        }
        if (entry.isExpired()) {
            LOGGER.log(Level.INFO, "[CACHE] Data expired in cache for key: {0} ", key);
            cache.remove(key);
            return Optional.empty();
        }
        LOGGER.log(Level.INFO, "[CACHE] Data retrieved from cache for key: {0} ", key);
        return Optional.of(entry.getValue());
    }

    public void evictByPrefix(String prefix) {
        cache.keySet().removeIf(key -> {
            if (key.startsWith(prefix)) {
                LOGGER.log(Level.INFO, "[CACHE] Data evicted from cache by prefix: {0} ", key);
                return true;
            }
            return false;
        });
    }

    public void evict(String key) {
        cache.remove(key);
        LOGGER.log(Level.INFO, "[CACHE] Data evicted from cache for key: {0} ", key);
    }

    public void invalidateUserCache(Long userId) {
        evict("user_" + userId);
        evictByPrefix("user_by_iban_");
        evictByPrefix("users_by_role_");
        evict("all_users");
    }

    public void clear() {
        cache.clear();
        LOGGER.info("[CACHE] Cache cleared");
    }

    public void cleanUp() {
        cache.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                LOGGER.log(Level.INFO, "[CACHE] Expired data removed for key: {0} ", entry.getKey());
                return true;
            }
            return false;
        });
    }
}
