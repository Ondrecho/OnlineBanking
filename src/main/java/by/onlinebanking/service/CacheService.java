package by.onlinebanking.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class CacheService {
    private final Map<String, CacheEntry> cache = new HashMap<>();

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

    public void put(String key, Object value) {
        cache.put(key, new CacheEntry(value));
    }

    public Optional<Object> get(String key) {
        CacheEntry entry = cache.get(key);

        if (entry == null || entry.isExpired()) {
            cache.remove(key);
            return Optional.empty();
        }

        return Optional.of(entry.getValue());
    }

    public void evictByPrefix(String prefix) {
        cache.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public void evict(String key) {
        cache.remove(key);
    }

    public void invalidateUserCache(Long userId) {
        evict("user_" + userId); // Кэш пользователя по ID
        evictByPrefix("user_by_iban_"); // Кэш пользователя по IBAN
        evictByPrefix("users_by_role_"); // Кэш пользователей по роли
        evict("all_users"); // Кэш всех пользователей
    }

    public void clear() {
        cache.clear();
    }

    public void cleanUp() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
