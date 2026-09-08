package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import ru.practicum.shareit.user.model.User;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> storage = new HashMap<>();
    private final Map<String, Long> emailIndex = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGen.getAndIncrement());
        } else {
            User existing = storage.get(user.getId());
            if (existing != null && existing.getEmail() != null && !existing.getEmail().equals(user.getEmail())) {
                emailIndex.computeIfPresent(existing.getEmail(), (k, v) -> v.equals(user.getId()) ? null : v);
            }
        }

        storage.put(user.getId(), user);
        emailIndex.put(user.getEmail(), user.getId());
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(Long id) {
        User removed = storage.remove(id);
        if (removed != null) {
            emailIndex.remove(removed.getEmail());
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return emailIndex.containsKey(email);
    }

}
