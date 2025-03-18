package by.onlinebanking.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String errorCode;
    private final String message;
    private final Map<String, Object> details;

    public ErrorResponse(String errorCode, String message, Map<String, Object> details) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
    }
}
