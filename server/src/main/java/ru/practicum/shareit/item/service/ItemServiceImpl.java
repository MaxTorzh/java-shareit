package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.validator.ItemValidator;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
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
        return saveItem(item, "создании вещи");
    }

    @Override
    @Transactional
    public Item updateItem(Long itemId, Item item) {
        Item existingItem = getItemById(itemId);
        validator.updateItemFields(existingItem, item);
        return saveItem(existingItem, "обновлении вещи");
    }

    @Override
    public Item getItemById(Long itemId) {
        return repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    @Override
    public Page<Item> getUserItems(Long userId, Pageable pageable) {
        service.getUserById(userId);
        return repository.findByOwnerId(userId, pageable);
    }

    @Override
    public Page<Item> searchItems(String text, Pageable pageable) {
        return repository.searchAvailableItems(text, pageable);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        getItemById(itemId);
        repository.deleteById(itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public Item getItemByIdWithDependencies(Long itemId) {
        return repository.findItemWithDependencies(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> getItemsWithDependencies(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.findItemsWithDependenciesByIds(itemIds);
    }

    private Item saveItem(Item item, String operation) {
        try {
            return repository.save(item);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Ошибка при " + operation + ": " + item.getName());
        }
    }
}
