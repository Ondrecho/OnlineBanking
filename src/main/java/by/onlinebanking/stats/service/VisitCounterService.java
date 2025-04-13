package by.onlinebanking.stats.service;

import by.onlinebanking.stats.VisitStats;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterService {
    private final VisitStats visitStats = new VisitStats();

    public synchronized void recordVisit(String url) {
        visitStats.recordVisit(url);
    }

    public synchronized long getVisitCount(String url) {
        return visitStats.getVisitCount(url);
    }

    public synchronized Map<String, Long> getAllStats() {
        return visitStats.getAllStats();
    }
}
