package ru.practicum.shareit.user.service;

import jakarta.validation.Valid;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto dto);

    UserDto update(Long id, @Valid UserUpdateDto dto);

    UserDto getById(Long id);

    List<UserDto> getAll();

    void delete(Long id);
}
