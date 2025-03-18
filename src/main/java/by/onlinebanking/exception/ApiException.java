package by.onlinebanking.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final String errorCode;
    private final transient Map<String, Object> details;

    public ApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }

    public ApiException(String errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ApiException addDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
}
