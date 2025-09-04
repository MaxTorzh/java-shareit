package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {

    ItemRequest createRequest(ItemRequest itemRequest);

    ItemRequest getRequestById(Long requestId);

    List<ItemRequest> getUserRequests(Long userId);

    List<ItemRequest> getAllRequests();

    List<ItemRequest> getOtherUserRequests(Long userId);
}
