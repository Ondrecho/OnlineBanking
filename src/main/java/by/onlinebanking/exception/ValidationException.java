package by.onlinebanking.exception;

import java.util.Map;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String message, Map<String, Object> details) {
        super("VALIDATION_ERROR", message, details);
    }
}

