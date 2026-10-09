package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createShouldSaveRequest() {
        User user = createUser("Ivan", "ivan@mail.ru");

        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Need drill");

        ItemRequestDto result = itemRequestService.create(user.getId(), dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need drill");
        assertThat(result.getItems()).isEmpty();
        assertThat(itemRequestRepository.count()).isEqualTo(1);
    }

    @Test
    void createShouldThrowWhenUserNotFound() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Need drill");

        assertThatThrownBy(() -> itemRequestService.create(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwnRequestsShouldReturnRequestsWithItemsInDescOrder() {
        User user = createUser("Ivan", "ivan@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");

        ItemRequest oldRequest = createRequest(user, "Old request", LocalDateTime.now().minusHours(2));
        ItemRequest newRequest = createRequest(user, "New request", LocalDateTime.now().minusHours(1));

        createItem(owner, "Item for old request", oldRequest);
        createItem(owner, "Item for new request", newRequest);

        List<ItemRequestDto> result = itemRequestService.getOwnRequests(user.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(newRequest.getId());
        assertThat(result.get(1).getId()).isEqualTo(oldRequest.getId());

        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(1).getItems()).hasSize(1);
    }

    @Test
    void getOwnRequestsShouldReturnEmptyListWhenNoRequests() {
        User user = createUser("Ivan", "ivan@mail.ru");

        List<ItemRequestDto> result = itemRequestService.getOwnRequests(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getOwnRequestsShouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getOwnRequests(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllRequestsShouldReturnOtherUsersRequestsOnly() {
        User user = createUser("Ivan", "ivan@mail.ru");
        User other = createUser("Petr", "petr@mail.ru");

        ItemRequest otherRequest = createRequest(other, "Other request", LocalDateTime.now().minusHours(2));

        List<ItemRequestShortDto> result = itemRequestService.getAllRequests(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(otherRequest.getId());
        assertThat(result.get(0).getDescription()).isEqualTo("Other request");
    }

    @Test
    void getAllRequestsShouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getAllRequests(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByIdShouldReturnRequestWithItems() {
        User viewer = createUser("Viewer", "viewer@mail.ru");
        User owner = createUser("Owner", "owner@mail.ru");

        ItemRequest request = createRequest(viewer, "Need drill", LocalDateTime.now().minusHours(1));
        createItem(owner, "Drill", request);

        ItemRequestDto result = itemRequestService.getById(viewer.getId(), request.getId());

        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getDescription()).isEqualTo("Need drill");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Drill");
        assertThat(result.getItems().get(0).getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void getByIdShouldThrowWhenRequestNotFound() {
        User user = createUser("Ivan", "ivan@mail.ru");

        assertThatThrownBy(() -> itemRequestService.getById(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByIdShouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getById(999L, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private ItemRequest createRequest(User requestor, String description, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setRequestor(requestor);
        request.setDescription(description);
        request.setCreated(created);
        return itemRequestRepository.save(request);
    }

    private Item createItem(User owner, String name, ItemRequest request) {
        Item item = new Item();
        item.setOwner(owner);
        item.setName(name);
        item.setDescription(name + " description");
        item.setAvailable(true);
        item.setRequest(request);
        return itemRepository.save(item);
    }
}