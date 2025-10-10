package ru.practicum.shareit.request.validator;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

@Component
public class ItemRequestValidator {

    public void validateItemRequestCreation(ItemRequestRequestDto requestDto) {
        if (requestDto.getDescription() == null || requestDto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }
    }

    public void validateRequestId(Long requestId) {
        if (requestId == null || requestId <= 0) {
            throw new ValidationException("ID запроса должен быть положительным числом");
        }
    }

    public void validatePagination(Integer from, Integer size) {
        if (from == null || from < 0) {
            throw new ValidationException("Параметр from должен быть неотрицательным");
        }
        if (size == null || size <= 0) {
            throw new ValidationException("Параметр size должен быть положительным");
        }
    }
}

