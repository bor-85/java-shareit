package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createShouldSaveBookingAndReturnDto() {
        User booker = createUser("Booker", "booker@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", true);

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        BookingDto result = bookingService.create(booker.getId(), dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(bookingRepository.count()).isEqualTo(1);
    }

    @Test
    void createShouldThrowWhenUserNotFound() {
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", true);

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        assertThatThrownBy(() -> bookingService.create(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createShouldThrowWhenItemNotFound() {
        User booker = createUser("Booker", "booker@mail.ru");

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(999L);
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createShouldThrowWhenBookerTriesToBookOwnItem() {
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", true);

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        assertThatThrownBy(() -> bookingService.create(owner.getId(), dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createShouldThrowWhenItemNotAvailable() {
        User booker = createUser("Booker", "booker@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", false);

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createShouldThrowWhenStartIsNull() {
        User booker = createUser("Booker", "booker@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", true);

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setEnd(LocalDateTime.now().plusHours(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createShouldThrowWhenEndIsBeforeStart() {
        User booker = createUser("Booker", "booker@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", true);

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusHours(2));
        dto.setEnd(LocalDateTime.now().plusHours(1));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approveShouldApproveByOwner() {
        User booker = createUser("Booker", "booker@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", true);

        Booking booking = createBooking(booker, item,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                Booking.Status.WAITING);

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(approved.getId()).isEqualTo(booking.getId());
        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);

        Booking saved = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(Booking.Status.APPROVED);
    }

    @Test
    void approveShouldThrowWhenNotOwner() {
        User booker = createUser("Booker", "booker@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");
        User stranger = createUser("Stranger", "stranger@mail.ru");
        Item item = createItem(owner, "Drill", true);

        Booking booking = createBooking(booker, item,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                Booking.Status.WAITING);

        assertThatThrownBy(() -> bookingService.approve(stranger.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approveShouldThrowWhenBookingIsNotWaiting() {
        User booker = createUser("Booker", "booker@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", true);

        Booking booking = createBooking(booker, item,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                Booking.Status.APPROVED);

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), booking.getId(), true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getByIdShouldReturnBookingForBookerAndOwner() {
        User booker = createUser("Booker", "booker@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", true);

        Booking booking = createBooking(booker, item,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                Booking.Status.WAITING);

        BookingDto byBooker = bookingService.getById(booker.getId(), booking.getId());
        BookingDto byOwner = bookingService.getById(owner.getId(), booking.getId());

        assertThat(byBooker.getId()).isEqualTo(booking.getId());
        assertThat(byOwner.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getByIdShouldThrowWhenUserIsNeitherBookerNorOwner() {
        User booker = createUser("Booker", "booker@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");
        User stranger = createUser("Stranger", "stranger@mail.ru");
        Item item = createItem(owner, "Drill", true);

        Booking booking = createBooking(booker, item,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                Booking.Status.WAITING);

        assertThatThrownBy(() -> bookingService.getById(stranger.getId(), booking.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByBookerShouldReturnCurrentBookings() {
        User booker = createUser("Booker", "booker@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", true);

        Booking current = createBooking(booker, item,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                Booking.Status.WAITING);

        createBooking(booker, item,
                LocalDateTime.now().minusHours(4),
                LocalDateTime.now().minusHours(2),
                Booking.Status.WAITING);

        createBooking(booker, item,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(4),
                Booking.Status.WAITING);

        List<BookingDto> result = bookingService.getByBooker(booker.getId(), "CURRENT");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(current.getId());
    }

    @Test
    void getByOwnerShouldReturnRejectedBookings() {
        User booker = createUser("Booker", "booker@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", true);

        Booking rejected = createBooking(booker, item,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                Booking.Status.REJECTED);

        createBooking(booker, item,
                LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(4),
                Booking.Status.WAITING);

        List<BookingDto> result = bookingService.getByOwner(owner.getId(), "REJECTED");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(rejected.getId());
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item createItem(User owner, String name, boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(name + " description");
        item.setAvailable(available);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    private Booking createBooking(User booker, Item item,
                                  LocalDateTime start,
                                  LocalDateTime end,
                                  Booking.Status status) {
        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }
}