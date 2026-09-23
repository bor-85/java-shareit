package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static ru.practicum.shareit.exception.BookingValidationMessages.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final String STATE_ALL = "ALL";
    private static final String STATE_CURRENT = "CURRENT";
    private static final String STATE_PAST = "PAST";
    private static final String STATE_FUTURE = "FUTURE";
    private static final String STATE_WAITING = "WAITING";
    private static final String STATE_REJECTED = "REJECTED";

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDto create(Long userId, BookingCreateDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + userId));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException(ERROR_ITEM_NOT_FOUND + dto.getItemId()));

        validateCreate(dto.getStart(), dto.getEnd());

        if (item.getOwner() != null && Objects.equals(item.getOwner().getId(), userId)) {
            throw new ValidationException(ERROR_OWNER_CANNOT_BOOK_OWN_ITEM);
        }

        if (!item.isAvailable()) {
            throw new ValidationException(ERROR_ITEM_NOT_AVAILABLE);
        }

        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Booking.Status.WAITING);

        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ERROR_BOOKING_NOT_FOUND + bookingId));

        if (booking.getItem() == null
                || booking.getItem().getOwner() == null) {
            throw new NotFoundException(ERROR_BOOKING_NOT_FOUND + bookingId);
        }

        if (!Objects.equals(booking.getItem().getOwner().getId(), ownerId)) {
            throw new ForbiddenException(ERROR_ONLY_OWNER_CAN_APPROVE);
        }

        if (booking.getStatus() != Booking.Status.WAITING) {
            throw new ValidationException(ERROR_ONLY_WAITING_CAN_BE_APPROVED);
        }

        booking.setStatus(approved ? Booking.Status.APPROVED : Booking.Status.REJECTED);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(ERROR_BOOKING_NOT_FOUND + bookingId));

        boolean isBooker = booking.getBooker() != null
                && Objects.equals(booking.getBooker().getId(), userId);

        boolean isOwner = booking.getItem() != null
                && booking.getItem().getOwner() != null
                && Objects.equals(booking.getItem().getOwner().getId(), userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException(ERROR_BOOKING_NOT_FOUND + bookingId);
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getByBooker(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + userId));

        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
        return filterByState(bookings, state);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getByOwner(Long ownerId, String state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(ERROR_USER_NOT_FOUND + ownerId));

        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId);
        return filterByState(bookings, state);
    }

    private List<BookingDto> filterByState(List<Booking> bookings, String state) {
        String normalizedState = normalizeState(state);
        LocalDateTime now = LocalDateTime.now();

        return bookings.stream()
                .filter(booking -> matchesState(booking, normalizedState, now))
                .map(bookingMapper::toDto)
                .toList();
    }

    private boolean matchesState(Booking booking, String state, LocalDateTime now) {
        return switch (state) {
            case STATE_ALL -> true;

            case STATE_CURRENT -> booking.getStatus() == Booking.Status.APPROVED
                    && !booking.getStart().isAfter(now)
                    && !booking.getEnd().isBefore(now);

            case STATE_PAST -> booking.getStatus() == Booking.Status.APPROVED
                    && booking.getEnd().isBefore(now);

            case STATE_FUTURE -> booking.getStatus() == Booking.Status.APPROVED
                    && booking.getStart().isAfter(now);

            case STATE_WAITING -> booking.getStatus() == Booking.Status.WAITING;

            case STATE_REJECTED -> booking.getStatus() == Booking.Status.REJECTED;

            default -> throw new ValidationException(ERROR_UNKNOWN_STATE + state);
        };
    }

    private String normalizeState(String state) {
        if (state == null || state.isBlank()) {
            return STATE_ALL;
        }
        return state.trim().toUpperCase(Locale.ROOT);
    }

    private void validateCreate(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            throw new ValidationException(ERROR_BOOKING_START_REQUIRED);
        }
        if (end == null) {
            throw new ValidationException(ERROR_BOOKING_END_REQUIRED);
        }
        if (!start.isBefore(end)) {
            throw new ValidationException(ERROR_BOOKING_DATE_ORDER);
        }
        if (!start.isAfter(LocalDateTime.now())) {
            throw new ValidationException(ERROR_BOOKING_START_IN_FUTURE);
        }
    }
}