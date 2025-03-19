package by.onlinebanking.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ResponseDto {
    private String message;
    private LocalDateTime timeStamp;
    private HttpStatus status;
}
