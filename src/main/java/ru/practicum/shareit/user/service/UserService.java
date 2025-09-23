package ru.practicum.shareit.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    User createUser(User user);

    User updateUser(Long userId, User user);

    User getUserById(Long userId);

    Page<User> getAllUsers(Pageable pageable);

    void deleteUser(Long userId);
}

