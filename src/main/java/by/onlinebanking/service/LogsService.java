package by.onlinebanking.service;

import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.exception.ValidationException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class LogsService {
    private static final long TASK_TTL_MINUTES = 30;
    private static final String LOGS = "logs_";
    private static final String TASK_ID = "taskId";
    private static final String STATUS = "status";
    @Value("${logging.file.path:logs/application.current.log}")
    private String logFilePath;
    private final Map<String, TaskWrapper> tasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupOldTasks, 30, 30, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void cleanup() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public String createLogFileAsync(String date) {
        String taskId = UUID.randomUUID().toString();
        CompletableFuture<LogFileResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                List<String> logs = getLogsForDate(LocalDate.parse(date));

                if (logs.isEmpty()) {
                    return new LogFileResult(new ByteArrayResource(new byte[0]),
                            LOGS + date + ".log");
                }

                String content = String.join("\n", logs);
                ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
                    @Override
                    public String getFilename() {
                        return LOGS + date + ".log";
                    }
                };
                return new LogFileResult(resource, LOGS + date + ".log");
            } catch (Exception e) {
                throw new CompletionException("Failed to create log file", e);
            }
        });

        future.whenComplete((result, ex) -> {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(() -> tasks.remove(taskId), 1, TimeUnit.HOURS);
            executor.shutdown();
        });
        tasks.put(taskId, new TaskWrapper(future));
        return taskId;
    }

    public Map<String, Object> getTaskStatus(String taskId) {
        TaskWrapper wrapper = tasks.get(taskId);
        if (wrapper == null) {
            throw new NotFoundException("Task not found or expired")
                    .addDetail(TASK_ID, taskId);
        }
        Map<String, Object> response = new HashMap<>();

        if (wrapper.future.isDone()) {
            try {
                LogFileResult result = wrapper.future.get();
                response.put(STATUS, "COMPLETED");
                response.put("hasLogs", !result.isEmpty());
                response.put("filename", result.getFilename());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                response.put(STATUS, "FAILED");
                response.put("error", "Task interrupted");
            } catch (Exception e) {
                response.put(STATUS, "FAILED");
                response.put("error", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            }
        } else {
            response.put(STATUS, "PENDING");
        }

        return response;
    }

    public LogFileResult getTaskLog(String taskId) throws IOException {
        TaskWrapper wrapper = tasks.get(taskId);
        if (wrapper == null) {
            throw new NotFoundException("Task not found")
                    .addDetail(TASK_ID, taskId);
        }
        if (!wrapper.future.isDone()) {
            throw new BusinessException("Task is not completed")
                    .addDetail(TASK_ID, taskId);
        }
        try {
            return wrapper.future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Task interrupted", e);
        } catch (ExecutionException e) {
            tasks.remove(taskId);
            throw new IOException("Failed to get log file", e.getCause());
        } catch (CancellationException e) {
            tasks.remove(taskId);
            throw new IOException("Task was cancelled", e);
        }
    }

    private void cleanupOldTasks() {
        Iterator<Map.Entry<String, TaskWrapper>> iterator = tasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TaskWrapper> entry = iterator.next();
            TaskWrapper wrapper = entry.getValue();

            if (wrapper.future.isDone() && wrapper.isOlderThan()) {
                iterator.remove();
            }
        }
    }

    public List<String> getLogsForDate(LocalDate date) {
        try {
            return getLogFilesForDate(date).stream()
                    .flatMap(this::readLinesSafely)
                    .filter(line -> isLineDateMatch(line, date))
                    .toList();
        } catch (IOException e) {
            return Collections.emptyList();
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

    @Getter
    public static class LogFileResult {
        private final ByteArrayResource resource;
        private final String filename;
        private final boolean isEmpty;

        public LogFileResult(ByteArrayResource resource, String filename) {
            this.resource = resource;
            this.filename = filename;
            this.isEmpty = resource.contentLength() == 0;
        }

        public ByteArrayResource getResource() {
            if (isEmpty) {
                throw new IllegalStateException("No resource available (empty logs)");
            }
            return resource;
        }

        public long getContentLength() {
            return isEmpty ? 0 : resource.contentLength();
        }
    }

    private static class TaskWrapper {
        final CompletableFuture<LogFileResult> future;
        final long creationTime;

        TaskWrapper(CompletableFuture<LogFileResult> future) {
            this.future = future;
            this.creationTime = System.currentTimeMillis();
        }

        boolean isOlderThan() {
            long ageMillis = System.currentTimeMillis() - creationTime;
            return ageMillis > TimeUnit.MINUTES.toMillis(LogsService.TASK_TTL_MINUTES);
        }
    }
}