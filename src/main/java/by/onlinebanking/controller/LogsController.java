package by.onlinebanking.controller;

import by.onlinebanking.exception.ValidationException;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class LogsController {
    @Value("${logging.file.path:logs/application.log}")
    private String logFilePath;

    @GetMapping(value = "/api/logs", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<ByteArrayResource> getLogsByDate(
            @RequestParam @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}",
                    message = "Invalid data format (yyyy-MM-dd)") String date
    ) throws IOException {
        LocalDate targetDate = parseDate(date);
        validateDateNotInFuture(targetDate);

        List<String> logs = collectLogs(targetDate);
        String content = String.join("\n", logs);

        ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "logs_" + date + ".log";
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename())
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(content.getBytes().length)
                .body(resource);
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Invalid data");
        }
    }

    private void validateDateNotInFuture(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Data can not be in the future");
        }
    }

    private List<String> collectLogs(LocalDate targetDate) throws IOException {
        List<Path> logFiles = getLogFilesInOrder();
        List<String> result = new ArrayList<>();

        for (Path file : logFiles) {
            try (Stream<String> lines = Files.lines(file)) {
                for (String line : lines.toList()) {
                    if (line.length() < 10 || !line.substring(0, 10).matches("\\d{4}-\\d{2}-\\d{2}")) {
                        continue;
                    }

                    String lineDateStr = line.substring(0, 10);
                    try {
                        LocalDate lineDate = LocalDate.parse(lineDateStr);
                        if (lineDate.isEqual(targetDate)) {
                            result.add(line);
                        }
                    } catch (DateTimeParseException ignore) {
                        // do not proceed
                    }
                }
            }
        }
        return result;
    }

    private List<Path> getLogFilesInOrder() throws IOException {
        Path mainLogFile = Paths.get(logFilePath);
        Path logDir = mainLogFile.getParent();
        String baseName = mainLogFile.getFileName().toString();

        try (Stream<Path> pathStream = Files.list(logDir)) {
            return pathStream
                    .filter(path -> path.getFileName().toString().startsWith(baseName))
                    .sorted(Comparator.reverseOrder())
                    .toList();
        }
    }
}