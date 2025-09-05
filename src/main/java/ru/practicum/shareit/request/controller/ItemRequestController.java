package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private final UserService userService;

    @PostMapping
    public ItemRequestDto createItemRequest(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                            @RequestHeader("X-Sharer-User-Id") Long requesterId) {
        return ItemRequestMapper.toDto(
                itemRequestService.createRequest(ItemRequestMapper.toItemRequest(itemRequestDto,
                        userService.getUserById(requesterId)))
        );
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@PathVariable Long requestId,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ItemRequestMapper.toDto(itemRequestService.getRequestById(requestId));
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getUserRequests(userId).stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllRequests().stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }
}
