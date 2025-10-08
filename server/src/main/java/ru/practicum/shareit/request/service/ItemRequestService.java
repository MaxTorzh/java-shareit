package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestsDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {

    ItemRequest createRequest(ItemRequest itemRequest);

    ItemRequest getRequestById(Long requestId);

    Page<ItemRequest> getUserRequests(Long userId, Pageable pageable);

    Page<ItemRequest> getAllRequests(Pageable pageable);

    Page<ItemRequest> getOtherUserRequests(Long userId, Pageable pageable);

    ItemRequestsDto getItemRequestDtoById(Long requestId);

    ItemRequest getItemRequestByIdWithDependencies(Long requestId);

    List<ItemRequest> getItemRequestsWithDependencies(List<Long> requestIds);

    List<ItemRequestsDto> getUserRequestsDtoList(Long userId, Pageable pageable);

    List<ItemRequestsDto> getAllRequestsDtoList(Pageable pageable);

    List<ItemRequestsDto> getOtherUserRequestsDtoList(Long userId, Pageable pageable);
}
