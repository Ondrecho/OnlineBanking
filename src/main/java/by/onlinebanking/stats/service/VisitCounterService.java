package by.onlinebanking.stats.service;

import by.onlinebanking.stats.VisitStats;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterService {
    private final VisitStats visitStats;

    public VisitCounterService(VisitStats visitStats) {
        this.visitStats = visitStats;
    }

    public void recordVisit(String url) {
        visitStats.recordVisit(url);
    }

    public long getVisitCount(String url) {
        return visitStats.getVisitCount(url);
    }

    public Map<String, Long> getAllStats() {
        return visitStats.getAllStats();
    }
}
