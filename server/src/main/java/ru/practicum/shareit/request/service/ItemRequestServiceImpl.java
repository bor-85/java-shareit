package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static ru.practicum.shareit.validation.UserValidationMessages.ERROR_USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper mapper;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + userId));

        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());

        ItemRequest saved = itemRequestRepository.save(request);
        return mapper.toDto(saved, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + userId));

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ItemRequestItemDto>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(groupingBy(
                        item -> item.getRequest().getId(),
                        mapping(mapper::toItemDto, toList())
                ));

        return requests.stream()
                .map(request -> mapper.toDto(request, itemsByRequestId.getOrDefault(request.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestShortDto> getAllRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + userId));

        return itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId).stream()
                .map(mapper::toShortDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto getById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + userId));

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Не найден запрос с id = " + requestId));

        List<ItemRequestItemDto> items = itemRepository.findByRequestId(requestId).stream()
                .map(mapper::toItemDto)
                .toList();

        return mapper.toDto(request, items);
    }
}
