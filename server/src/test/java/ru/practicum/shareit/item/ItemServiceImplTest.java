package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.practicum.shareit.validation.CommentValidationMessages.ERROR_COMMENT_NOT_ALLOWED;
import static ru.practicum.shareit.validation.ItemValidationMessages.ERROR_ITEM_NOT_FOUND;
import static ru.practicum.shareit.validation.UserValidationMessages.ERROR_USER_NOT_FOUND;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void createShouldSaveItemForOwner() {
        User owner = createUser("Owner", "owner@mail.ru");

        ItemDto dto = new ItemDto();
        dto.setName("Drill");
        dto.setDescription("Power drill");
        dto.setAvailable(true);

        ItemDto created = itemService.create(owner.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Drill");
        assertThat(created.getDescription()).isEqualTo("Power drill");
        assertThat(created.getAvailable()).isTrue();
        assertThat(created.getRequestId()).isNull();
        assertThat(created.getComments()).isEmpty();

        assertThat(itemRepository.count()).isEqualTo(1);
    }

    @Test
    void createShouldThrowWhenOwnerNotFound() {
        ItemDto dto = new ItemDto();
        dto.setName("Drill");
        dto.setDescription("Power drill");
        dto.setAvailable(true);

        assertThatThrownBy(() -> itemService.create(999L, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ERROR_USER_NOT_FOUND);
    }

    @Test
    void updateShouldChangeOnlyProvidedFields() {
        User owner = createUser("Owner", "owner@mail.ru");
        Item item = createItem(owner, "Drill", "Power drill", true);

        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName("New drill");
        dto.setAvailable(false);

        ItemDto updated = itemService.update(owner.getId(), item.getId(), dto);

        assertThat(updated.getId()).isEqualTo(item.getId());
        assertThat(updated.getName()).isEqualTo("New drill");
        assertThat(updated.getDescription()).isEqualTo("Power drill");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void updateShouldThrowWhenItemNotFound() {
        User owner = createUser("Owner", "owner@mail.ru");

        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName("New name");

        assertThatThrownBy(() -> itemService.update(owner.getId(), 999L, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ERROR_ITEM_NOT_FOUND);
    }

    @Test
    void updateShouldThrowWhenUserIsNotOwner() {
        User owner = createUser("Owner", "owner@mail.ru");
        User stranger = createUser("Stranger", "stranger@mail.ru");
        Item item = createItem(owner, "Drill", "Power drill", true);

        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName("New name");

        assertThatThrownBy(() -> itemService.update(stranger.getId(), item.getId(), dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ERROR_ITEM_NOT_FOUND);
    }

    @Test
    void getByIdShouldReturnItemWithComments() {
        User owner = createUser("Owner", "owner@mail.ru");
        User author = createUser("Author", "author@mail.ru");
        Item item = createItem(owner, "Drill", "Power drill", true);

        createComment(author, item, "Great item", LocalDateTime.now().minusHours(1));
        createComment(author, item, "Second comment", LocalDateTime.now().minusMinutes(30));

        ItemDto result = itemService.getById(item.getId());

        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getComments()).hasSize(2);
        assertThat(result.getComments().get(0).getText()).isEqualTo("Second comment");
        assertThat(result.getComments().get(1).getText()).isEqualTo("Great item");
    }

    @Test
    void getAllForOwnerShouldReturnItemWithLastAndNextBookings() {
        User owner = createUser("Owner", "owner@mail.ru");
        User booker = createUser("Booker", "booker@mail.ru");
        Item item = createItem(owner, "Drill", "Power drill", true);

        Booking pastBooking = createBooking(booker, item,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                Booking.Status.APPROVED);

        Booking futureBooking = createBooking(booker, item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                Booking.Status.APPROVED);

        createComment(booker, item, "Nice item", LocalDateTime.now().minusHours(1));

        List<ItemDto> result = itemService.getAllForOwner(owner.getId());

        assertThat(result).hasSize(1);

        ItemDto dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getText()).isEqualTo("Nice item");

        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getLastBooking().getId()).isEqualTo(pastBooking.getId());
        assertThat(dto.getLastBooking().getBookerId()).isEqualTo(booker.getId());

        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getNextBooking().getId()).isEqualTo(futureBooking.getId());
        assertThat(dto.getNextBooking().getBookerId()).isEqualTo(booker.getId());
    }

    @Test
    void getAllForOwnerShouldThrowWhenOwnerNotFound() {
        assertThatThrownBy(() -> itemService.getAllForOwner(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ERROR_USER_NOT_FOUND);
    }

    @Test
    void searchShouldReturnOnlyAvailableMatchingItems() {
        User owner = createUser("Owner", "owner@mail.ru");

        createItem(owner, "Drill", "Power drill", true);
        createItem(owner, "Hammer", "Heavy hammer", false);
        createItem(owner, "Screwdriver", "Tool for screws", true);

        List<ItemDto> result = itemService.search("drill");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void searchShouldReturnEmptyListForBlankText() {
        List<ItemDto> result = itemService.search("   ");

        assertThat(result).isEmpty();
    }

    @Test
    void addCommentShouldSaveCommentWhenUserHadApprovedPastBooking() {
        User owner = createUser("Owner", "owner@mail.ru");
        User booker = createUser("Booker", "booker@mail.ru");
        Item item = createItem(owner, "Drill", "Power drill", true);

        createBooking(booker, item,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                Booking.Status.APPROVED);

        CommentCreateDto dto = new CommentCreateDto();
        dto.setText("Nice item");

        CommentDto result = itemService.addComment(booker.getId(), item.getId(), dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getText()).isEqualTo("Nice item");
        assertThat(result.getAuthorName()).isEqualTo("Booker");

        assertThat(commentRepository.count()).isEqualTo(1);
    }

    @Test
    void addCommentShouldThrowWhenUserHasNoApprovedPastBooking() {
        User owner = createUser("Owner", "owner@mail.ru");
        User booker = createUser("Booker", "booker@mail.ru");
        Item item = createItem(owner, "Drill", "Power drill", true);

        CommentCreateDto dto = new CommentCreateDto();
        dto.setText("Nice item");

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), item.getId(), dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(ERROR_COMMENT_NOT_ALLOWED);
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item createItem(User owner, String name, String description, boolean available) {
        Item item = new Item();
        item.setOwner(owner);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
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

    private Comment createComment(User author, Item item, String text, LocalDateTime created) {
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setText(text);
        comment.setCreated(created);
        return commentRepository.save(comment);
    }
}