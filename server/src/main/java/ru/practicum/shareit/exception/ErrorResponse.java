package ru.practicum.shareit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String error;
    private String timeStamp;

    public ErrorResponse(String error) {
        this.error = error;
        this.timeStamp = java.time.LocalDateTime.now().toString();
    }
}
