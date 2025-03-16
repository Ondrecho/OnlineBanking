package by.onlinebanking.utils;

import by.onlinebanking.service.CacheService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheCleanupTask {
    private final CacheService cacheService;

    @Autowired
    public CacheCleanupTask(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.MINUTES)
    public void cleanUpCache() {
        cacheService.cleanUp();
    }
}
