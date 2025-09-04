package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public Item createItem(Item item) {
        userService.getUserById(item.getOwner().getId());
        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(Long itemId, Item item) {
        Item existingItem = getItemById(itemId);
        updateItemFields(existingItem, item);
        return itemRepository.save(existingItem);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    @Override
    public List<Item> getUserItems(Long userId) {
        userService.getUserById(userId);
        return itemRepository.findByOwnerId(userId);
    }

    @Override
    public List<Item> searchItems(String text) {
        return itemRepository.search(text);
    }

    @Override
    public void deleteItem(Long itemId) {
        getItemById(itemId);
        itemRepository.deleteById(itemId);
    }

    private void updateItemFields(Item existingItem, Item newItem) {
        if (newItem.getName() != null) {
            existingItem.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            existingItem.setDescription(newItem.getDescription());
        }
        if (newItem.getIsAvailable() != null) {
            existingItem.setIsAvailable(newItem.getIsAvailable());
        }
    }
}
