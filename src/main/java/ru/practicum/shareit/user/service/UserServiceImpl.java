package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.validator.UserValidator;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserValidator validator;

    @Override
    @Transactional
    public User createUser(User user) {
        validator.checkUniqueEmail(user.getEmail());
        return saveUser(user, "создании пользователя");
    }

    @Override
    @Transactional
    public User updateUser(Long userId, User user) {
        User existingUser = getUserById(userId);
        validator.updateUserFields(existingUser, user);
        return saveUser(existingUser, "обновлении пользователя");
    }

    @Override
    public User getUserById(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        validator.validateExistingUser(userId);
        repository.deleteById(userId);
    }

    private User saveUser(User user, String operation) {
        try {
            return repository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Ошибка при " + operation + ": " + user.getEmail());
        }
    }
}
