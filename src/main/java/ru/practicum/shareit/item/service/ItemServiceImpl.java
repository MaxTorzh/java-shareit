package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.validator.ItemValidator;
import ru.practicum.shareit.user.service.UserService;

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
    public Page<Item> getUserItems(Long userId, Pageable pageable) {
        service.getUserById(userId);
        return repository.findByOwnerId(userId, pageable);
    }

    @Override
    @Transactional
    public Page<Item> searchItems(String text, Pageable pageable) {
        if (text == null || text.isBlank()) {
            return Page.empty();
        }
        return repository.searchAvailableItems(text, pageable);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        getItemById(itemId);
        repository.deleteById(itemId);
    }
}
