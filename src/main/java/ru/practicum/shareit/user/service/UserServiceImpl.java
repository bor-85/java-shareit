package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Objects;

import static ru.practicum.shareit.exception.UserValidationMessages.ERROR_EMAIL_ALREADY_EXISTS;
import static ru.practicum.shareit.exception.UserValidationMessages.ERROR_USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException(ERROR_EMAIL_ALREADY_EXISTS + dto.getEmail());
        }
        User user = userMapper.toUser(dto);
        User saved = userRepository.save(user);
        return userMapper.toUserDto(saved);
    }

    @Override
    public UserDto update(Long id, UserDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND + id));

        if (!Objects.equals(existing.getEmail(), dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException(ERROR_EMAIL_ALREADY_EXISTS + dto.getEmail());
        }

        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());

        User saved = userRepository.save(existing);
        return userMapper.toUserDto(saved);
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND + id));
        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND + id));

        userRepository.deleteById(id);
    }
}
