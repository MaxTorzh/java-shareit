package ru.practicum.shareit.request.repository;

import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository {

    ItemRequest save(ItemRequest itemRequest);

    Optional<ItemRequest> findById(Long itemRequestId);

    List<ItemRequest> findByRequesterId(Long requesterId);

    List<ItemRequest> findAll();

    List<ItemRequest> findAllExceptRequester(Long requesterId);
}
