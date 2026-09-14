package ru.practicum.shareit.user.storage;

import java.util.List;
import java.util.Optional;
import ru.practicum.shareit.user.model.User;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    void deleteById(Long id);

    boolean existsByEmail(String email);

}
