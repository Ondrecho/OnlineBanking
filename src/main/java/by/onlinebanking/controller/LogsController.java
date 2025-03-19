package by.onlinebanking.controller;

import by.onlinebanking.exception.ValidationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class LogsController {
    @Value("${logging.file.dir:logs}")
    private String logDir;

    @GetMapping("/api/logs")
    public ResponseEntity<Page<String>> getLogsByDate(
            @RequestParam
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Invalid date format. Use yyyy-MM-dd")
            String date,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number cannot be negative")
            int page,

            @RequestParam(defaultValue = "100")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 1000, message = "Page size cannot exceed 1000")
            int size) throws IOException {
        LocalDate parsedDate = validateDate(date);

        List<Path> logFiles = findLogFiles(date, parsedDate);

        if (logFiles.isEmpty()) {
            return ResponseEntity.ok(new PageImpl<>(Collections.emptyList()));
        }

        List<String> allLines = readAllLines(logFiles);

        int totalPages = (int) Math.ceil((double) allLines.size() / size);
        if (page >= totalPages && totalPages > 0) {
            throw new ValidationException("Page number out of range.")
                    .addDetail("pagesAllowed", (totalPages - 1));
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        int total = allLines.size();
        int start = Math.min((int) pageRequest.getOffset(), total);
        int end = Math.min((int) pageRequest.getOffset() + pageRequest.getPageSize(), total);

        return ResponseEntity.ok(new PageImpl<>(
                allLines.subList(start, end),
                pageRequest,
                total
        ));
    }

    private LocalDate validateDate(String date) {
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            if (parsedDate.isAfter(LocalDate.now())) {
                throw new ValidationException("Future dates are not allowed");
            }
            return parsedDate;
        } catch (Exception e) {
            throw new ValidationException("Invalid date value");
        }
    }

    private List<Path> findLogFiles(String dateString, LocalDate date) throws IOException {
        LocalDate today = LocalDate.now();

        try (Stream<Path> paths = Files.list(Paths.get(logDir))) {
            return paths
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        boolean isCurrent = fileName.equals("application.current.log") && date.equals(today);
                        boolean isArchived = fileName.matches("application\\." +
                                dateString +
                                "(\\.\\d+)?.log");
                        return isCurrent || isArchived;
                    })
                    .sorted()
                    .toList();
        }
    }

    private List<String> readAllLines(List<Path> logFiles) {
        return logFiles.stream()
                .flatMap(file -> {
                    try {
                        return Files.readAllLines(file).stream();
                    } catch (IOException e) {
                        return Stream.empty();
                    }
                })
                .toList();
    }
}
