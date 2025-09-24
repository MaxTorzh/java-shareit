package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.model.ItemRequest;

public interface ItemRequestService {

    ItemRequest createRequest(ItemRequest itemRequest);

    ItemRequest getRequestById(Long requestId);

    Page<ItemRequest> getUserRequests(Long userId, Pageable pageable);

    Page<ItemRequest> getAllRequests(Pageable pageable);

    Page<ItemRequest> getOtherUserRequests(Long userId, Pageable pageable);
}