package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.ItemRequest;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    Page<ItemRequest> findByRequesterId(Long requesterId, Pageable pageable);

    @Query("SELECT ir FROM ItemRequest ir WHERE ir.requester.id <> :requesterId")
    Page<ItemRequest> findAllExceptRequester(@Param("requesterId") Long requesterId, Pageable pageable);
}
