package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestsDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final ItemRequestMapper itemRequestMapper;

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

    @Override
    public ItemRequestsDto getItemRequestDtoById(Long requestId) {
        // Используем оптимизированный метод с JOIN FETCH
        ItemRequest request = getItemRequestByIdWithDependencies(requestId);
        ItemRequestsDto dto = itemRequestMapper.toDto(request);
        populateItemsForRequestsWithDependencies(dto);
        return dto;
    }

    @Override
    public ItemRequest getItemRequestByIdWithDependencies(Long requestId) {
        return itemRequestRepository.findItemRequestWithRequester(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID " + requestId + " не найден"));
    }

    @Override
    public List<ItemRequest> getItemRequestsWithDependencies(List<Long> requestIds) {
        if (requestIds.isEmpty()) {
            return new ArrayList<>();
        }
        return itemRequestRepository.findAllWithRequesterByIds(requestIds);
    }

    @Override
    public List<ItemRequestsDto> getUserRequestsDtoList(Long userId, Pageable pageable) {
        Page<ItemRequest> requestsPage = getUserRequests(userId, pageable);

        List<Long> requestIds = requestsPage.getContent().stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<ItemRequest> requestsWithDependencies = getItemRequestsWithDependencies(requestIds);

        return requestsWithDependencies.stream()
                .map(request -> {
                    ItemRequestsDto dto = itemRequestMapper.toDto(request);
                    populateItemsForRequestsWithDependencies(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestsDto> getAllRequestsDtoList(Pageable pageable) {
        Page<ItemRequest> requestsPage = getAllRequests(pageable);

        List<Long> requestIds = requestsPage.getContent().stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<ItemRequest> requestsWithDependencies = getItemRequestsWithDependencies(requestIds);

        return requestsWithDependencies.stream()
                .map(request -> {
                    ItemRequestsDto dto = itemRequestMapper.toDto(request);
                    populateItemsForRequestsWithDependencies(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestsDto> getOtherUserRequestsDtoList(Long userId, Pageable pageable) {
        Page<ItemRequest> requestsPage = getOtherUserRequests(userId, pageable);

        List<Long> requestIds = requestsPage.getContent().stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<ItemRequest> requestsWithDependencies = getItemRequestsWithDependencies(requestIds);

        return requestsWithDependencies.stream()
                .map(request -> {
                    ItemRequestsDto dto = itemRequestMapper.toDto(request);
                    populateItemsForRequestsWithDependencies(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void populateItemsForRequestsWithDependencies(ItemRequestsDto dto) {
        List<Item> items = itemRepository.findItemsWithDependenciesByRequestId(dto.getId());
        List<ItemDto> itemDtos = items.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);
    }
}
