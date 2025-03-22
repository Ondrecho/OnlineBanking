package by.onlinebanking.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed",
                errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "BUSINESS_ERROR" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };

        ErrorResponse response = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getDetails()
        );
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                Map.of("cause", ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex
    ) {
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof UnrecognizedPropertyException unrecognizedPropertyEx) {
            return handleUnrecognizedPropertyException(unrecognizedPropertyEx);
        }
        if (rootCause instanceof InvalidFormatException invalidFormatEx) {
            return handleInvalidFormatException(invalidFormatEx);
        }

        ErrorResponse response = new ErrorResponse(
                "INVALID_REQUEST",
                "Invalid request: Malformed JSON",
                Map.of("cause", "The request contains invalid JSON")
        );
        return ResponseEntity.badRequest().body(response);
    }

    private ResponseEntity<ErrorResponse> handleInvalidFormatException(InvalidFormatException ex) {
        String fieldName = ex.getPath().get(0).getFieldName();
        String providedValue = ex.getValue().toString();
        String expectedFormat = "yyyy-MM-dd";

        ErrorResponse response = new ErrorResponse(
                "INVALID_REQUEST",
                "Invalid request: Incorrect date format",
                Map.of(
                        "field", fieldName,
                        "expectedFormat", expectedFormat,
                        "providedValue", providedValue
                )
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleUnrecognizedPropertyException(
            UnrecognizedPropertyException ex
    ) {
        String unknownField = ex.getPropertyName();
        String message = "Invalid request: Unknown field '" + unknownField + "'";

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_ERROR",
                message,
                Map.of("unknownFields", Collections.singletonList(unknownField))
        );

        return ResponseEntity.badRequest().body(response);
    }
}