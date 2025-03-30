package by.onlinebanking.service;

import by.onlinebanking.exception.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LogsService {
    @Value("${logging.file.path:logs/application.current.log}")
    private String logFilePath;

    public List<String> getLogsForDate(LocalDate date) {
        try {
            return getLogFilesForDate(date).stream()
                    .flatMap(this::readLinesSafely)
                    .filter(line -> isLineDateMatch(line, date))
                    .toList();
        } catch (IOException e) {
            return Collections.emptyList(); // Обрабатываем IOException
        }
    }

    private Stream<String> readLinesSafely(Path file) {
        try {
            return Files.lines(file);
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    private boolean isLineDateMatch(String line, LocalDate targetDate) {
        return line.length() >= 10 && line.startsWith(targetDate.toString());
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

    private List<Path> getLogFilesForDate(LocalDate targetDate) throws IOException {
        Path logDir = Paths.get(logFilePath).getParent();
        boolean isToday = targetDate.equals(LocalDate.now());

        try (Stream<Path> paths = Files.list(logDir)) {
            List<Path> files = paths
                    .filter(path -> isRelevantLogFile(path, targetDate))
                    .sorted(this::compareLogFiles)
                    .toList();

            if (isToday) {
                Path currentFile = Paths.get(logFilePath);
                if (Files.exists(currentFile)) {
                    List<Path> result = new ArrayList<>(files);
                    result.add(currentFile);
                    return result;
                }
            }
            return files;
        }
    }

    private boolean isRelevantLogFile(Path path, LocalDate targetDate) {
        String fileName = path.getFileName().toString();
        return fileName.matches("application\\." + targetDate + "\\.\\d+\\.log");
    }

    private int compareLogFiles(Path p1, Path p2) {
        String n1 = p1.getFileName().toString();
        String n2 = p2.getFileName().toString();
        return extractFileIndex(n1) - extractFileIndex(n2);
    }

    private int extractFileIndex(String filename) {
        Matcher m = Pattern.compile("\\.(\\d+)\\.log$").matcher(filename);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }
}