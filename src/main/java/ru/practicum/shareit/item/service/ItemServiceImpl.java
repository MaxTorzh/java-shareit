package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.validator.ItemValidator;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserService service;
    private final ItemValidator validator;

    @Override
    @Transactional
    public Item createItem(Item item) {
        service.getUserById(item.getOwner().getId());
        validator.validateItemCreation(item);
        return validator.saveItem(item, "создании вещи");
    }

    @Override
    @Transactional
    public Item updateItem(Long itemId, Item item) {
        Item existingItem = getItemById(itemId);
        validator.updateItemFields(existingItem, item);
        return validator.saveItem(existingItem, "обновлении вещи");
    }

    @Override
    @Transactional
    public Item getItemById(Long itemId) {
        return repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    @Override
    @Transactional
    public List<Item> getUserItems(Long userId) {
        service.getUserById(userId);
        return repository.findByOwnerId(userId);
    }

    @Override
    @Transactional
    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return repository.searchAvailableItems(text);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        getItemById(itemId);
        repository.deleteById(itemId);
    }
}
