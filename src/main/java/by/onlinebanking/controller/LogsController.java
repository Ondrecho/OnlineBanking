package by.onlinebanking.controller;

import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.logging.service.LogsService;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
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
        try {
            LogsService.LogFileResult result = logsService.getTaskLog(taskId);

            if (result.isEmpty()) {
                throw new NotFoundException("No logs found for the specified date");
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.getFilename())
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(result.getContentLength())
                    .body(result.getResource());
        } catch (Exception e) {
            throw new BusinessException("Error while getting log file")
                    .addDetail("taskId", taskId)
                    .addDetail("error", e.getMessage());
        }
    }

    @GetMapping("/{taskId}/status")
    public ResponseEntity<Map<String, Object>> getTaskStatusByTaskId(@PathVariable String taskId) {
        Map<String, Object> response = logsService.getTaskStatus(taskId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createLogFileAsync(@RequestParam @Pattern(
            regexp = "\\d{4}-\\d{2}-\\d{2}",
            message = "Invalid date format (yyyy-MM-dd)"
    ) String date) {
        LocalDate targetDate = logsService.parseDate(date);
        logsService.validateDateNotInFuture(targetDate);
        String taskId = logsService.createLogFileAsync(targetDate);

        return ResponseEntity.accepted().body(Map.of("taskId", taskId));
    }
}