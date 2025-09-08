package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

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
    public List<ItemRequest> getUserRequests(Long userId) {
        userService.getUserById(userId);
        return itemRequestRepository.findByRequesterId(userId);
    }

    @Override
    public List<ItemRequest> getAllRequests() {
        return itemRequestRepository.findAll();
    }

    @Override
    public List<ItemRequest> getOtherUserRequests(Long userId) {
        userService.getUserById(userId);
        return itemRequestRepository.findAllExceptRequester(userId);
    }
}
