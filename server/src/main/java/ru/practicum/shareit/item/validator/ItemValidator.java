package ru.practicum.shareit.item.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

@Component
@RequiredArgsConstructor
public class ItemValidator {
    private final UserService service;

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
    }
}
