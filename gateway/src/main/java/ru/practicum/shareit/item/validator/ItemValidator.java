package ru.practicum.shareit.item.validator;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemRequestDto;

@Component
public class ItemValidator {

    public void validateItemCreation(ItemRequestDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название обязательно");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание обязательно");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности обязателен");
        }
    }

    public void validateItemId(Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw new ValidationException("ID вещи должен быть положительным числом");
        }
    }

    public void validateSearchText(String text) {
        if (text == null) {
            throw new ValidationException("Текст для поиска не может быть null");
        }
    }

    public void validateComment(CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }
    }
}
