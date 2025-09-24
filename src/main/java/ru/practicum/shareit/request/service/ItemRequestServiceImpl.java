package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;

    @Override
    public ItemRequest createRequest(ItemRequest itemRequest) {
        userService.getUserById(itemRequest.getRequester().getId());
        itemRequest.setCreatedTime(LocalDateTime.now());
        return itemRequestRepository.save(itemRequest);
    }

    @Override
    public ItemRequest getRequestById(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID " + requestId + " не найден"));
    }

    @Override
    public Page<ItemRequest> getUserRequests(Long userId, Pageable pageable) {
        userService.getUserById(userId);
        return itemRequestRepository.findByRequesterId(userId, pageable);
    }

    @Override
    public Page<ItemRequest> getAllRequests(Pageable pageable) {
        return itemRequestRepository.findAll(pageable);
    }

    @Override
    public Page<ItemRequest> getOtherUserRequests(Long userId, Pageable pageable) {
        userService.getUserById(userId);
        return itemRequestRepository.findAllExceptRequester(userId, pageable);
    }
}
