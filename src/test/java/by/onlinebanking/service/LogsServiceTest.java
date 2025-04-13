package by.onlinebanking.service;

import by.onlinebanking.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogsServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void getLogsForDate_ReturnsFilteredLogs() throws Exception {
        LogsService service = new LogsService();
        LocalDate date = LocalDate.of(2023, 1, 1);

        Path logFile = tempDir.resolve("application.2023-01-01.1.log");
        Files.write(logFile, List.of(
                "2023-01-01 10:00:00 - Valid log",
                "2023-01-02 10:00:00 - Invalid date log"
        ));

        setPrivateField(service, "logFilePath", tempDir.resolve("application.current.log").toString());

        List<String> result = service.getLogsForDate(date);

        assertEquals(1, result.size());
        assertTrue(result.get(0).contains("Valid log"));
    }

    @Test
    void getLogsForDate_WhenIOException_ReturnsEmptyList() throws Exception {
        LogsService service = new LogsService();
        LocalDate date = LocalDate.of(2023, 1, 1);

        setPrivateField(service, "logFilePath", "/nonexistent/path.log");

        List<String> result = service.getLogsForDate(date);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseDate_InvalidDate_ThrowsException() {
        LogsService service = new LogsService();
        Exception exception = assertThrows(ValidationException.class,
                () -> service.parseDate("invalid-date"));
        assertEquals("Invalid date format", exception.getMessage());
    }

    @Test
    void parseDate_InvalidFormat_ThrowsValidationException() {
        LogsService service = new LogsService();

        Exception exception = assertThrows(ValidationException.class,
                () -> service.parseDate("invalid-date-format"));

        assertEquals("Invalid date format", exception.getMessage());
    }

    @Test
    void validateDateNotInFuture_FutureDate_ThrowsException() {
        LogsService service = new LogsService();
        LocalDate futureDate = LocalDate.now().plusDays(1);

        Exception exception = assertThrows(ValidationException.class,
                () -> service.validateDateNotInFuture(futureDate));
        assertEquals("Date cannot be in the future", exception.getMessage());
    }

    @Test
    void getLogFilesForDate_IncludesCurrentFileWhenExists() throws Exception {
        LogsService service = new LogsService();
        LocalDate today = LocalDate.now();

        Path currentFile = tempDir.resolve("application.current.log");
        Files.createFile(currentFile);
        setPrivateField(service, "logFilePath", currentFile.toString());

        List<Path> result = invokePrivateMethod(service,
                "getLogFilesForDate",
                new Class[]{LocalDate.class},
                today);

        assertTrue(result.contains(currentFile));
    }

    @Test
    void getLogFilesForDate_ExcludesCurrentFileWhenNotExists() throws Exception {
        LogsService service = new LogsService();
        LocalDate today = LocalDate.now();

        Path currentFile = tempDir.resolve("application.current.log");
        setPrivateField(service, "logFilePath", currentFile.toString());

        List<Path> result = invokePrivateMethod(service,
                "getLogFilesForDate",
                new Class[]{LocalDate.class},
                today);

        assertFalse(result.contains(currentFile));
    }

    @Test
    void readLinesSafely_WhenIOException_ReturnsEmptyStream() throws Exception {
        LogsService service = new LogsService();
        Path unreadableFile = tempDir.resolve("unreadable.log");
        Files.createFile(unreadableFile);

        unreadableFile.toFile().setReadable(false);

        try {
            Stream<String> result = invokePrivateMethod(service,
                    "readLinesSafely",
                    new Class[]{Path.class},
                    unreadableFile);
            assertEquals(0, result.count());
        } finally {
            unreadableFile.toFile().setReadable(true);
        }
    }

    @Test
    void isLineDateMatch_VariousScenarios() throws Exception {
        LogsService service = new LogsService();
        LocalDate date = LocalDate.of(2023, 1, 1);

        assertTrue((Boolean)invokePrivateMethod(service,
                "isLineDateMatch",
                new Class[]{String.class, LocalDate.class},
                "2023-01-01 10:00:00 - Log", date));

        assertFalse((Boolean)invokePrivateMethod(service,
                "isLineDateMatch",
                new Class[]{String.class, LocalDate.class},
                "2023-01-02 10:00:00 - Log", date));

        assertFalse((Boolean)invokePrivateMethod(service,
                "isLineDateMatch",
                new Class[]{String.class, LocalDate.class},
                "Short", date));
    }

    @Test
    void compareLogFiles_ComparesCorrectly() throws Exception {
        LogsService service = new LogsService();

        Path p1 = mock(Path.class);
        Path p2 = mock(Path.class);
        when(p1.getFileName()).thenReturn(Paths.get("app.1.log"));
        when(p2.getFileName()).thenReturn(Paths.get("app.2.log"));

        int result = invokePrivateMethod(service,
                "compareLogFiles",
                new Class[]{Path.class, Path.class},
                p1, p2);

        assertTrue(result < 0);
    }

    @Test
    void extractFileIndex_ExtractsCorrectly() throws Exception {
        LogsService service = new LogsService();

        int index1 = invokePrivateMethod(service,
                "extractFileIndex",
                new Class[]{String.class},
                "application.2023-01-01.1.log");
        assertEquals(1, index1);

        int index2 = invokePrivateMethod(service,
                "extractFileIndex",
                new Class[]{String.class},
                "application.log");
        assertEquals(0, index2);
    }

    private <T> T invokePrivateMethod(Object target, String methodName,
                                      Class<?>[] paramTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return (T) method.invoke(target, args);
    }

    private void setPrivateField(Object target, String fieldName, Object value)
            throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}