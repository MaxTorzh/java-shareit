package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingListDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;
    private Long itemId;
    private String itemName;
    private Long bookerId;
    private String bookerName;
}
