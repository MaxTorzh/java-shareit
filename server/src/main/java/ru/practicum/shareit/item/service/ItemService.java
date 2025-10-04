package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;

import java.util.List;


public interface ItemService {

    Item createItem(Item item);

    Item updateItem(Long itemId, Item item);

    Item getItemById(Long itemId);

    Page<Item> getUserItems(Long userId, Pageable pageable);

    Page<Item> searchItems(String text, Pageable pageable);

    void deleteItem(Long itemId);

    Item getItemByIdWithDependencies(Long itemId);

    List<Item> getItemsWithDependencies(List<Long> itemIds);
}
