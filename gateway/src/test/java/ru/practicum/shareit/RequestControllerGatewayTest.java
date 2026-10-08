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
import ru.practicum.shareit.exception.GatewayExceptionHandler;
import ru.practicum.shareit.request.RequestClient;
import ru.practicum.shareit.request.RequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
@Import(GatewayExceptionHandler.class)
class RequestControllerGatewayTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestClient requestClient;

    @Test
    void createRequestShouldReturnCreatedRequest() throws Exception {
        ItemRequestCreateDto request = new ItemRequestCreateDto();
        request.setDescription("Need drill");

        ItemRequestDto response = new ItemRequestDto();
        response.setId(1L);
        response.setDescription("Need drill");
        response.setCreated(LocalDateTime.now());
        response.setItems(List.of());

        when(requestClient.create(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need drill"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0));

        verify(requestClient).create(eq(1L), any(ItemRequestCreateDto.class));
    }

    @Test
    void createRequestShouldReturnBadRequestForInvalidBody() throws Exception {
        String json = """
                {
                  "description": " "
                }
                """;

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(requestClient);
    }

    @Test
    void getOwnRequestsShouldReturnRequests() throws Exception {
        ItemRequestDto dto1 = buildRequestDto(1L, "Need drill");
        ItemRequestDto dto2 = buildRequestDto(2L, "Need hammer");

        when(requestClient.getOwnRequests(1L))
                .thenReturn(ResponseEntity.ok(List.of(dto1, dto2)));

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(requestClient).getOwnRequests(1L);
    }

    @Test
    void getAllRequestsShouldReturnShortRequests() throws Exception {
        ItemRequestShortDto shortDto = new ItemRequestShortDto();
        shortDto.setId(1L);
        shortDto.setDescription("Need drill");
        shortDto.setCreated(LocalDateTime.now());

        when(requestClient.getAllRequests(1L))
                .thenReturn(ResponseEntity.ok(List.of(shortDto)));

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Need drill"));

        verify(requestClient).getAllRequests(1L);
    }

    @Test
    void getRequestByIdShouldReturnRequest() throws Exception {
        ItemRequestDto dto = buildRequestDto(1L, "Need drill");

        when(requestClient.getById(1L, 10L))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(get("/requests/{requestId}", 10L)
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need drill"));

        verify(requestClient).getById(1L, 10L);
    }

    private ItemRequestDto buildRequestDto(Long id, String description) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(id);
        dto.setDescription(description);
        dto.setCreated(LocalDateTime.now());

        ItemRequestItemDto item = new ItemRequestItemDto();
        item.setId(100L);
        item.setName("Drill");
        item.setOwnerId(200L);

        dto.setItems(List.of(item));
        return dto;
    }
}