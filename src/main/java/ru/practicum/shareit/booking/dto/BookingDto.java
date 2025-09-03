package ru.practicum.shareit.booking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long itemId;
    private Long bookerId;
    private String status;
}
