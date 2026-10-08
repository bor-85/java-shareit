package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUserShouldReturnCreatedUser() throws Exception {
        UserDto request = new UserDto();
        request.setName("Ivan");
        request.setEmail("ivan@mail.ru");

        UserDto response = new UserDto();
        response.setId(1L);
        response.setName("Ivan");
        response.setEmail("ivan@mail.ru");

        when(userService.create(any(UserDto.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));

        verify(userService).create(any(UserDto.class));
    }

    @Test
    void createUserShouldReturnBadRequestForInvalidBody() throws Exception {
        String json = """
                {
                  "name": "",
                  "email": "wrong-email"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserShouldReturnUpdatedUser() throws Exception {
        UserUpdateDto request = new UserUpdateDto();
        request.setName("Ivan Updated");
        request.setEmail("ivan.updated@mail.ru");

        UserDto response = new UserDto();
        response.setId(1L);
        response.setName("Ivan Updated");
        response.setEmail("ivan.updated@mail.ru");

        when(userService.update(eq(1L), any(UserUpdateDto.class))).thenReturn(response);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan Updated"))
                .andExpect(jsonPath("$.email").value("ivan.updated@mail.ru"));

        verify(userService).update(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    void getUserByIdShouldReturnUser() throws Exception {
        UserDto response = new UserDto();
        response.setId(1L);
        response.setName("Ivan");
        response.setEmail("ivan@mail.ru");

        when(userService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));

        verify(userService).getById(1L);
    }

    @Test
    void getAllUsersShouldReturnUsers() throws Exception {
        UserDto user1 = new UserDto();
        user1.setId(1L);
        user1.setName("Ivan");
        user1.setEmail("ivan@mail.ru");

        UserDto user2 = new UserDto();
        user2.setId(2L);
        user2.setName("Petr");
        user2.setEmail("petr@mail.ru");

        when(userService.getAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(userService).getAll();
    }

    @Test
    void deleteUserShouldCallService() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }
}