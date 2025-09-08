package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    Item createItem(Item item);

    Item updateItem(Long itemId, Item item);

    Item getItemById(Long itemId);

    List<Item> getUserItems(Long userId);

    List<Item> searchItems(String text);

    void deleteItem(Long itemId);
}
