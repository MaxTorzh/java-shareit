package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.validator.UserValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    @Override
    @Transactional
    public User createUser(User user) {
        userValidator.checkUniqueEmail(user.getEmail());
        return userValidator.saveUser(user, "создании пользователя");
    }

    @Override
    public User updateUser(Long userId, User user) {
        User existingUser = getUserById(userId);
        userValidator.updateUserFields(existingUser, user);
        return userValidator.saveUser(existingUser, "обновлении пользователя");
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long userId) {
        userValidator.validateExistingUser(userId);
        userRepository.deleteById(userId);
    }
}
