package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.status.BookingState;
import ru.practicum.shareit.exception.ValidationException;

@Service
public class BookingStateService {
    public BookingState parseState(String state) {
        try {
            return state == null ? BookingState.ALL : BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
    }
}
