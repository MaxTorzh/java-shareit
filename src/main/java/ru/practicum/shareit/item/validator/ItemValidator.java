package ru.practicum.shareit.item.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

@Component
@RequiredArgsConstructor
public class ItemValidator {
    private final UserService service;
    private final ItemRepository repository;

    public void updateItemFields(Item existingItem, Item newItem) {
        if (newItem.getName() != null) {
            existingItem.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            existingItem.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            existingItem.setAvailable(newItem.getAvailable());
        }
    }

    public void validateItemCreation(Item item) {
        if (item.getOwner() == null || item.getOwner().getId() == null) {
            throw new ValidationException("Владелец обязателен");
        }
        service.getUserById(item.getOwner().getId());

        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Название обязательно");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Описание обязательно");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Статус доступности обязателен");
        }
    }

    public Item saveItem(Item item, String operation) {
        try {
            return repository.save(item);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Ошибка при " + operation + ": " + item.getName());
        }
    }
}
