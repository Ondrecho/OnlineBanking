package by.onlinebanking.controller;

import by.onlinebanking.stats.service.VisitCounterService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class VisitStatsController {
    private final VisitCounterService visitCounterService;

    public VisitStatsController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @PostMapping("/record")
    public void recordVisit(@RequestParam String url) {
        visitCounterService.recordVisit(url);
    }

    @GetMapping("/count")
    public long getVisitCount(@RequestParam String url) {
        return visitCounterService.getVisitCount(url);
    }

    @GetMapping("/all")
    public Map<String, Long> getAllStats() {
        return visitCounterService.getAllStats();
    }
}
