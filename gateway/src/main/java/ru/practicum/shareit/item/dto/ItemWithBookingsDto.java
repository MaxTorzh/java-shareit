package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDateDto;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithBookingsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long ownerId;
    private Long requestId;
    private BookingDateDto lastBooking;
    private BookingDateDto nextBooking;
    //private List<CommentDto> comments = new ArrayList<>();
}
