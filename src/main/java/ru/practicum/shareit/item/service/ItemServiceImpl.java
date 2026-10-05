package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.CommentValidationMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.shareit.exception.ItemValidationMessages.ERROR_ITEM_NOT_FOUND;
import static ru.practicum.shareit.exception.UserValidationMessages.ERROR_USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto create(Long ownerId, ItemDto dto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + ownerId));

        Item item = itemMapper.toItem(dto, owner);
        return toDtoWithComments(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemUpdateDto dto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ERROR_ITEM_NOT_FOUND + itemId));

        if (item.getOwner() == null || !ownerId.equals(item.getOwner().getId())) {
            throw new NotFoundException(ERROR_ITEM_NOT_FOUND + itemId);
        }

        if (dto.getName() != null) {
            if (dto.getName().isBlank()) {
                throw new ValidationException("Имя вещи не может быть пустым или состоять только из пробелов.");
            }
            item.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            if (dto.getDescription().isBlank()) {
                throw new ValidationException("Описание вещи не может быть пустым или состоять только из пробелов.");
            }
            item.setDescription(dto.getDescription());
        }

        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }

        return toDtoWithComments(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ERROR_ITEM_NOT_FOUND + itemId));

        ItemDto dto = itemMapper.toItemDto(item);
        dto.setComments(commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(commentMapper::toDto)
                .toList());

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllForOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + ownerId));

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        Map<Long, List<CommentDto>> commentsByItemId = commentRepository
                .findAllByItemIdsOrderByCreatedDesc(itemIds)
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        java.util.stream.Collectors.mapping(commentMapper::toDto, java.util.stream.Collectors.toList())
                ));

        LocalDateTime now = LocalDateTime.now();

        Map<Long, BookingShortDto> lastBookingByItemId =
                buildLastBookingMap(
                        bookingRepository.findByItemIdInAndStatusOrderByEndDesc(itemIds, Booking.Status.APPROVED),
                        now
                );

        Map<Long, BookingShortDto> nextBookingByItemId =
                buildNextBookingMap(
                        bookingRepository.findByItemIdInAndStatusOrderByStartAsc(itemIds, Booking.Status.APPROVED),
                        now
                );

        return items.stream()
                .map(item -> toOwnerItemDto(item, commentsByItemId, lastBookingByItemId, nextBookingByItemId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.searchAvailable(text.trim()).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + userId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ERROR_ITEM_NOT_FOUND + itemId));

        boolean canComment = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, itemId, Booking.Status.APPROVED, LocalDateTime.now());

        if (!canComment) {
            throw new ValidationException(CommentValidationMessages.ERROR_COMMENT_NOT_ALLOWED);
        }

        Comment comment = commentMapper.toEntity(dto, item, author);
        Comment saved = commentRepository.save(comment);

        return commentMapper.toDto(saved);
    }

    private ItemDto toDtoWithComments(Item item) {
        ItemDto dto = itemMapper.toItemDto(item);
        dto.setComments(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()).stream()
                .map(commentMapper::toDto)
                .toList());
        return dto;
    }

    private ItemDto toOwnerItemDto(
            Item item,
            Map<Long, List<CommentDto>> commentsByItemId,
            Map<Long, BookingShortDto> lastBookingByItemId,
            Map<Long, BookingShortDto> nextBookingByItemId
    ) {
        ItemDto dto = itemMapper.toItemDto(item);
        dto.setComments(commentsByItemId.getOrDefault(item.getId(), List.of()));
        dto.setLastBooking(lastBookingByItemId.get(item.getId()));
        dto.setNextBooking(nextBookingByItemId.get(item.getId()));
        return dto;
    }

    private Map<Long, BookingShortDto> buildLastBookingMap(List<Booking> bookings, LocalDateTime now) {
        Map<Long, BookingShortDto> result = new HashMap<>();

        for (Booking booking : bookings) {
            if (booking.getEnd().isBefore(now)) {
                result.putIfAbsent(booking.getItem().getId(), toShortDto(booking));
            }
        }

        return result;
    }

    private Map<Long, BookingShortDto> buildNextBookingMap(List<Booking> bookings, LocalDateTime now) {
        Map<Long, BookingShortDto> result = new HashMap<>();

        for (Booking booking : bookings) {
            if (booking.getStart().isAfter(now)) {
                result.putIfAbsent(booking.getItem().getId(), toShortDto(booking));
            }
        }

        return result;
    }

    private BookingShortDto toShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        return dto;
    }
}