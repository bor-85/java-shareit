package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.EmailDuplicatedException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

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
            throw new EmailDuplicatedException(ERROR_EMAIL_ALREADY_EXISTS + dto.getEmail());
        }
        User user = userMapper.toUser(dto);
        User saved = userRepository.save(user);
        return userMapper.toUserDto(saved);
    }

    @Override
    public UserDto update(Long id, UserUpdateDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + id));

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            if (!existing.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
                throw new EmailDuplicatedException(ERROR_EMAIL_ALREADY_EXISTS + dto.getEmail());
            }
            existing.setEmail(dto.getEmail());
        }

        return userMapper.toUserDto(userRepository.save(existing));
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + id));
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
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + id));

        userRepository.deleteById(id);
    }
}
