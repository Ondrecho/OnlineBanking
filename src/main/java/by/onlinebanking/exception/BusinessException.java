package by.onlinebanking.exception;

import java.util.Map;

public class BusinessException extends ApiException {
    public BusinessException(String message) {
        super("BUSINESS_ERROR", message);
    }

    public BusinessException(String message, Map<String, Object> details) {
        super("BUSINESS_ERROR", message, details);
    }
}
