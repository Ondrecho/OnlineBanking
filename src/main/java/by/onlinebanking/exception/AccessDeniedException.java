package by.onlinebanking.exception;

import java.util.Map;

public class AccessDeniedException extends ApiException {
    public AccessDeniedException(String message) {
        super("ACCESS_DENIED", message);
    }

    public AccessDeniedException(String message, Map<String, Object> details) {
        super("ACCESS_DENIED", message, details);
    }
}
