package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.GatewayExceptionHandler;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import(GatewayExceptionHandler.class)
class ItemControllerGatewayTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void createItemShouldReturnCreatedItem() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("Drill");
        request.setDescription("Power drill");
        request.setAvailable(true);
        request.setRequestId(10L);

        ItemDto response = buildItemDto(1L, "Drill", "Power drill", true, 10L);

        when(itemClient.create(eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.description").value("Power drill"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.requestId").value(10));

        verify(itemClient).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void createItemShouldReturnBadRequestForInvalidBody() throws Exception {
        String json = """
                {
                  "name": "",
                  "description": "",
                  "available": null
                }
                """;

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(itemClient);
    }

    @Test
    void updateItemShouldReturnUpdatedItem() throws Exception {
        ItemUpdateDto request = new ItemUpdateDto();
        request.setName("New drill");
        request.setDescription("New description");
        request.setAvailable(false);

        ItemDto response = buildItemDto(1L, "New drill", "New description", false, null);

        when(itemClient.update(eq(1L), eq(1L), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New drill"))
                .andExpect(jsonPath("$.description").value("New description"))
                .andExpect(jsonPath("$.available").value(false));

        verify(itemClient).update(eq(1L), eq(1L), any(ItemUpdateDto.class));
    }

    @Test
    void getItemByIdShouldReturnItem() throws Exception {
        ItemDto response = buildItemDto(1L, "Drill", "Power drill", true, null);

        when(itemClient.getById(1L))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/items/{itemId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.description").value("Power drill"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemClient).getById(1L);
    }

    @Test
    void getAllItemsShouldReturnItems() throws Exception {
        ItemDto item1 = buildItemDto(1L, "Drill", "Power drill", true, null);
        ItemDto item2 = buildItemDto(2L, "Hammer", "Heavy hammer", false, null);

        when(itemClient.getAll(1L))
                .thenReturn(ResponseEntity.ok(List.of(item1, item2)));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemClient).getAll(1L);
    }

    @Test
    void searchShouldReturnItems() throws Exception {
        ItemDto item = buildItemDto(1L, "Drill", "Power drill", true, null);

        when(itemClient.search("drill"))
                .thenReturn(ResponseEntity.ok(List.of(item)));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));

        verify(itemClient).search("drill");
    }

    @Test
    void addCommentShouldReturnCreatedComment() throws Exception {
        CommentCreateDto request = new CommentCreateDto();
        request.setText("Nice item");

        CommentDto response = new CommentDto();
        response.setId(1L);
        response.setText("Nice item");
        response.setAuthorName("Ivan");
        response.setCreated(LocalDateTime.now());

        when(itemClient.addComment(eq(1L), eq(1L), any(CommentCreateDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Nice item"))
                .andExpect(jsonPath("$.authorName").value("Ivan"));

        verify(itemClient).addComment(eq(1L), eq(1L), any(CommentCreateDto.class));
    }

    @Test
    void addCommentShouldReturnBadRequestForInvalidBody() throws Exception {
        String json = """
                {
                  "text": " "
                }
                """;

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(itemClient);
    }

    private ItemDto buildItemDto(Long id, String name, String description, boolean available, Long requestId) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        dto.setRequestId(requestId);
        dto.setComments(List.of());

        BookingShortDto lastBooking = new BookingShortDto();
        lastBooking.setId(10L);
        lastBooking.setBookerId(100L);

        BookingShortDto nextBooking = new BookingShortDto();
        nextBooking.setId(11L);
        nextBooking.setBookerId(101L);

        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        return dto;
    }
}