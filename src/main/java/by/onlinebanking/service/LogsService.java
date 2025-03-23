package by.onlinebanking.service;

import by.onlinebanking.exception.ValidationException;
import java.io.IOException;
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
import org.springframework.stereotype.Service;

@Service
public class LogsService {
    @Value("${logging.file.path:logs/application.log}")
    private String logFilePath;

    public List<String> collectLogs(LocalDate targetDate) throws IOException {
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
                        // ignoring lines that do not match the date format
                    }
                }
            }
        }
        return result;
    }

    public LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Invalid date format").addDetail("date", date);
        }
    }

    public void validateDateNotInFuture(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Date cannot be in the future").addDetail("date", date);
        }
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