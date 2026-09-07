package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ru.practicum.shareit.exception.ItemValidationMessages.ERROR_INVALID_OWNER;
import static ru.practicum.shareit.exception.ItemValidationMessages.ERROR_ITEM_NOT_FOUND;
import static ru.practicum.shareit.exception.UserValidationMessages.ERROR_USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto create(Long ownerId, ItemDto dto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND + ownerId));

        Item item = new Item();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.isAvailable());
        item.setOwner(owner);
        item.setRequest(null);

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto dto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_ITEM_NOT_FOUND + itemId));

        if (item.getOwner() == null || !ownerId.equals(item.getOwner().getId())) {
            throw new IllegalAccessError(ERROR_INVALID_OWNER);
        }

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.isAvailable());

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getById(Long itemId) {
        return itemRepository.findById(itemId)
                .map(itemMapper::toItemDto)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_ITEM_NOT_FOUND + itemId));
    }

    @Override
    public List<ItemDto> getAllForOwner(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.searchAvailable(text).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }
}
