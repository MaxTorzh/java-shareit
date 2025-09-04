package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        checkUniqueEmail(user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, User user) {
        User existingUser = getUserById(userId);
        updateUserFields(existingUser, user);
        return userRepository.save(existingUser);
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
        validateExistingUser(userId);
        userRepository.deleteById(userId);
    }

    private void updateUserFields(User existingUser, User newUser) {
        if (newUser.getName() != null) {
            existingUser.setName(newUser.getName());
        }
        if (newUser.getEmail() != null &&
        !newUser.getEmail().equals(existingUser.getEmail())) {
            checkUniqueEmail(newUser.getEmail());
            existingUser.setEmail(newUser.getEmail());
        }
    }

    private void validateExistingUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
    }

    private void checkUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Такой email уже существует: " + email);
        }
    }
}
