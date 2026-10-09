package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailDuplicatedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createShouldSaveUser() {
        UserDto dto = new UserDto();
        dto.setName("Ivan");
        dto.setEmail("ivan@mail.ru");

        UserDto created = userService.create(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Ivan");
        assertThat(created.getEmail()).isEqualTo("ivan@mail.ru");
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void createShouldThrowWhenEmailAlreadyExists() {
        userRepository.save(new User(null, "Ivan", "ivan@mail.ru"));

        UserDto dto = new UserDto();
        dto.setName("Petr");
        dto.setEmail("ivan@mail.ru");

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(EmailDuplicatedException.class);
    }

    @Test
    void updateShouldChangeNameAndEmail() {
        User saved = userRepository.save(new User(null, "Ivan", "ivan@mail.ru"));

        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("Ivan Updated");
        dto.setEmail("ivan.updated@mail.ru");

        UserDto updated = userService.update(saved.getId(), dto);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getName()).isEqualTo("Ivan Updated");
        assertThat(updated.getEmail()).isEqualTo("ivan.updated@mail.ru");
    }

    @Test
    void updateShouldThrowWhenUserNotFound() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("New name");
        dto.setEmail("new@mail.ru");

        assertThatThrownBy(() -> userService.update(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateShouldThrowWhenEmailAlreadyExists() {
        userRepository.save(new User(null, "Ivan", "ivan@mail.ru"));
        User saved = userRepository.save(new User(null, "Petr", "petr@mail.ru"));

        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("ivan@mail.ru");

        assertThatThrownBy(() -> userService.update(saved.getId(), dto))
                .isInstanceOf(EmailDuplicatedException.class);
    }

    @Test
    void getByIdShouldReturnUser() {
        User saved = userRepository.save(new User(null, "Ivan", "ivan@mail.ru"));

        UserDto result = userService.getById(saved.getId());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getName()).isEqualTo("Ivan");
        assertThat(result.getEmail()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void getByIdShouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllShouldReturnAllUsers() {
        userRepository.save(new User(null, "Ivan", "ivan@mail.ru"));
        userRepository.save(new User(null, "Petr", "petr@mail.ru"));

        List<UserDto> users = userService.getAll();

        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("ivan@mail.ru", "petr@mail.ru");
    }

    @Test
    void deleteShouldRemoveUser() {
        User saved = userRepository.save(new User(null, "Ivan", "ivan@mail.ru"));

        userService.delete(saved.getId());

        assertThat(userRepository.findById(saved.getId())).isEmpty();
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void deleteShouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }
}