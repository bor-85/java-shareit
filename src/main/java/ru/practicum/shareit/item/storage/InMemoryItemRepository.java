package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) item.setId(idGen.getAndIncrement());
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        List<Item> res = new ArrayList<>();
        for (Item item : storage.values()) {
            if (item.getOwner() != null && ownerId.equals(item.getOwner().getId())) {
                res.add(item);
            }
        }
        return res;
    }

    @Override
    public List<Item> searchAvailable(String text) {
        String q = text == null ? "" : text.trim().toLowerCase();
        if (q.isEmpty()) return List.of();

        List<Item> res = new ArrayList<>();
        for (Item item : storage.values()) {
            if (!item.isAvailable()) continue;
            String name = item.getName() == null ? "" : item.getName().toLowerCase();
            String desc = item.getDescription() == null ? "" : item.getDescription().toLowerCase();
            if (name.contains(q) || desc.contains(q)) {
                res.add(item);
            }
        }
        return res;
    }
}
