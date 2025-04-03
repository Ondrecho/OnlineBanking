package by.onlinebanking.controller;

import by.onlinebanking.service.LogsService;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequestMapping("/api/logs")
@RestController
public class LogsController {
    private final LogsService logsService;

    @Autowired
    public LogsController(LogsService logsService) {
        this.logsService = logsService;
    }

    @GetMapping(value = "/{taskId}/file", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<ByteArrayResource> getLogFileByTaskId(@PathVariable String taskId) {
        ByteArrayResource resource = logsService.getTaskLog(taskId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename())
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @GetMapping("/{taskId}/status")
    public ResponseEntity<Map<String, String>> getTaskStatusByTaskId(@PathVariable String taskId) {
        String status = logsService.getTaskStatus(taskId);
        return ResponseEntity.ok(Map.of("status", status));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createLogFileAsync(@RequestParam @Pattern(
            regexp = "\\d{4}-\\d{2}-\\d{2}",
            message = "Invalid date format (yyyy-MM-dd)"
    ) String date) {
        String taskId = logsService.createLogFileAsync(date);
        return ResponseEntity.accepted().body(Map.of("taskId", taskId));
    }
}