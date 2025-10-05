package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
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
