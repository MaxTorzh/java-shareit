package ru.practicum.shareit.request.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRequestRepository implements ItemRequestRepository {
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public ItemRequest save(ItemRequest itemRequest) {
        if (itemRequest.getId() == null) {
            itemRequest.setId(counter.getAndIncrement());
        }
        requests.put(itemRequest.getId(), itemRequest);
        return itemRequest;
    }

    @Override
    public Optional<ItemRequest> findById(Long itemRequestId) {
        return Optional.ofNullable(requests.get(itemRequestId));
    }

    @Override
    public List<ItemRequest> findByRequesterId(Long requesterId) {
        return requests.values().stream()
                .filter(request -> request.getRequester().getId().equals(requesterId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequest> findAll() {
        return new ArrayList<>(requests.values());
    }

    @Override
    public List<ItemRequest> findAllExceptRequester(Long requesterId) {
        return requests.values().stream()
                .filter(request -> !request.getRequester().getId().equals(requesterId))
                .collect(Collectors.toList());
    }
}
