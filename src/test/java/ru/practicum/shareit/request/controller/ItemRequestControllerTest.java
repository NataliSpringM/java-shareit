package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ItemRequestController tests
 */
@WebMvcTest(controllers = ItemRequestController.class)
@AutoConfigureMockMvc
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestService itemRequestService;
    Long itemRequestId;
    Long itemId;
    Long userId;
    Long requestId;
    String header;
    MediaType jsonType;


    @BeforeEach
    @SneakyThrows
    void before() {
        itemId = 1L;
        userId = 1L;
        requestId = 1L;
        itemRequestId = 1L;
        header = "X-Sharer-User-Id";
        jsonType = MediaType.APPLICATION_JSON;
    }

    /**
     * test create method
     * POST-request "/requests"
     * when request data are valid
     * should return status ok
     * should invoke service create method and return result
     */
    @Test
    @SneakyThrows
    void create_WhenItemRequestIsValid_StatusIsOk_AndInvokeService() {

        User user = User.builder().name("Olga").build();
        ItemDto item = ItemDto.builder().name("bike").build();
        List<ItemDto> items = List.of(item);
        ItemRequestDto validItemRequest = ItemRequestDto.builder()
                .description("I would like rent bike").build();
        String validItemRequestString = objectMapper.writeValueAsString(validItemRequest);
        ItemRequestOutDto itemRequest = ItemRequestMapper.toItemRequestOutDto(
                ItemRequestMapper.toItemRequest(validItemRequest, user), items).toBuilder().id(itemRequestId).build();
        String itemRequestString = objectMapper.writeValueAsString(itemRequest);


        when(itemRequestService.create(userId, validItemRequest)).thenReturn(itemRequest);

        String result = mockMvc.perform(post("/requests")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(validItemRequestString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequest.getId()), Long.class))
                .andExpect(jsonPath("$.created", is(itemRequest.getCreated().toString())))
                .andExpect(jsonPath("$.description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$.items.[0].name", is("bike")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemRequestService).create(userId, validItemRequest);
        assertEquals(itemRequestString, result);

    }

    /**
     * test create method
     * POST-request "/requests"
     * when request data are not valid: description is empty
     * should return status bad request
     * should not invoke service create method
     */

    @Test
    @SneakyThrows
    void create_WhenItemRequestHasEmptyDescription_StatusIsBadRequest_DoesNotInvokeService() {

        ItemRequestDto invalidItemRequest = ItemRequestDto.builder()
                .description("").build();
        String invalidItemRequestString = objectMapper.writeValueAsString(invalidItemRequest);

        mockMvc.perform(post("/requests")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidItemRequestString))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, Mockito.never()).create(anyLong(), any());
    }

    /**
     * test create method
     * POST-request "/requests"
     * when request data are not valid: description is null
     * should return status bad request
     * should not invoke service create method
     */

    @Test
    @SneakyThrows
    void create_WhenItemRequestHasNullDescription_StatusIsBadRequest_DoesNotInvokeService() {

        ItemRequestDto invalidItemRequest = ItemRequestDto.builder()
                .description(null).build();
        String invalidItemRequestString = objectMapper.writeValueAsString(invalidItemRequest);

        mockMvc.perform(post("/requests")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidItemRequestString))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, Mockito.never()).create(anyLong(), any());
    }

    /**
     * test getOwnRequests method
     * GET-request "/requests"
     * should return status ok
     * should invoke service getOwnRequests method and return result
     */

    @Test
    @SneakyThrows
    void getOwnRequests_IsStatusOk_AndInvokeService() {

        User user = User.builder().name("Olga").build();
        ItemDto item = ItemDto.builder().name("bike").build();
        List<ItemDto> items = List.of(item);
        ItemRequestDto validItemRequest = ItemRequestDto.builder()
                .description("I would like rent bike").build();

        ItemRequestOutDto itemRequest = ItemRequestMapper.toItemRequestOutDto(
                ItemRequestMapper.toItemRequest(validItemRequest, user), items).toBuilder().id(itemRequestId).build();

        List<ItemRequestOutDto> requests = List.of(itemRequest);
        String requestsListString = objectMapper.writeValueAsString(requests);

        when(itemRequestService.getOwnRequests(userId)).thenReturn(requests);

        String result = mockMvc.perform(get("/requests")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(itemRequest.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$.[0].created", is(itemRequest.getCreated().toString())))
                .andExpect(jsonPath("$.[0].items.[0].name", is("bike")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemRequestService).getOwnRequests(userId);
        assertEquals(result, requestsListString);
    }

    /**
     * test getOtherUserRequests method
     * GET-request "/requests/all"
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * <p>
     * when parameters are valid
     * should return status ok
     * should invoke service getOtherUserRequests method and return result
     */

    @Test
    @SneakyThrows
    void getOtherUsersRequests_WhenParametersAreValid_IsStatusOk_andInvokeService() {

        User user = User.builder().name("Olga").build();
        ItemDto item = ItemDto.builder().name("bike").build();
        List<ItemDto> items = List.of(item);
        ItemRequestDto validItemRequest = ItemRequestDto.builder()
                .description("I would like rent bike").build();

        ItemRequestOutDto itemRequest = ItemRequestMapper.toItemRequestOutDto(
                ItemRequestMapper.toItemRequest(validItemRequest, user), items).toBuilder().id(itemRequestId).build();

        List<ItemRequestOutDto> requests = List.of(itemRequest);
        String requestsListString = objectMapper.writeValueAsString(requests);

        String paramFromName = "from";
        Integer paramFromValue = 0;
        String paramSizeName = "size";
        Integer paramSizeValue = 10;

        when(itemRequestService.getOtherUsersRequests(userId, paramFromValue, paramSizeValue)).thenReturn(requests);

        String result = mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, String.valueOf(paramFromValue))
                        .param(paramSizeName, String.valueOf(paramSizeValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(itemRequest.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$.[0].created", is(itemRequest.getCreated().toString())))
                .andExpect(jsonPath("$.[0].items.[0].name", is("bike")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemRequestService).getOtherUsersRequests(userId, paramFromValue, paramSizeValue);
        assertEquals(result, requestsListString);
    }

    /**
     * test getOtherUserRequests method
     * GET-request "/requests/all"
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "from" is negative number, or when casting to number
     * should return status bad request
     * should not invoke service getOtherUserRequests method
     */
    @Test
    @SneakyThrows
    void getOtherUsersRequests_WhenFromIsNegative_IsStatusBadRequest_DoesNotInvokeService() {

        String paramFromName = "from";
        Integer paramFromValue = -1;
        String paramSizeName = "size";
        Integer paramSizeValue = 10;

        mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, String.valueOf(paramFromValue))
                        .param(paramSizeName, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getOtherUsersRequests(anyLong(), anyInt(), anyInt());
    }

    /**
     * test getOtherUserRequests method
     * GET-request "/requests/all"
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "size" is negative number, or when casting to number
     * should return status bad request
     * should not invoke service getOtherUserRequests method
     */
    @Test
    @SneakyThrows
    void getOtherUsersRequests_WhenSizeIsNegative_IsStatusBadRequest_DoesNotInvokeService() {

        String paramFromName = "from";
        Integer paramFromValue = 0;
        String paramSizeName = "size";
        Integer paramSizeValue = -1;

        mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, String.valueOf(paramFromValue))
                        .param(paramSizeName, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getOtherUsersRequests(anyLong(), anyInt(), anyInt());
    }

    /**
     * test getOtherUserRequests method
     * GET-request "/requests/all"
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "size" is zero number, or when casting to number
     * should return status bad request
     * should not invoke service getOtherUserRequests method
     */
    @Test
    @SneakyThrows
    void getOtherUsersRequests_WhenSizeIsZero_IsStatusBadRequest_AndDoesNotInvokeService() {

        String paramFromName = "from";
        Integer paramFromValue = 0;
        String paramSizeName = "size";
        Integer paramSizeValue = 0;

        mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, String.valueOf(paramFromValue))
                        .param(paramSizeName, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getOtherUsersRequests(anyLong(), anyInt(), anyInt());
    }

    /**
     * test getOtherUserRequests method
     * GET-request "/requests/all"
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "from" is not convertible to number
     * should return status internal server error
     * should not invoke service getOtherUserRequests method
     */
    @Test
    @SneakyThrows
    void getOtherUsersRequests_WhenFromIsNotNumber_IsStatusInternalServerError_DoesNotInvokeService() {

        String paramFromName = "from";
        String paramFromValue = "from";
        String paramSizeName = "size";
        Integer paramSizeValue = 10;

        mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, paramFromValue)
                        .param(paramSizeName, String.valueOf(paramSizeValue)))
                .andExpect(status().isInternalServerError());

        verify(itemRequestService, never()).getOtherUsersRequests(anyLong(), anyInt(), anyInt());

    }

    /**
     * test getOtherUserRequests method
     * GET-request "/requests/all"
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "size" is not convertible to number
     * should return status internal server error
     * should not invoke service getOtherUserRequests method
     */
    @Test
    @SneakyThrows
    void getOtherUsersRequests_WhenSizeIsNotNumber_IsStatusInternalServerError_DoesNotInvokeService() {

        String paramFromName = "from";
        String paramFromValue = "10";
        String paramSizeName = "size";
        String paramSizeValue = "size";

        mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, paramFromValue)
                        .param(paramSizeName, paramSizeValue))
                .andExpect(status().isInternalServerError());

        verify(itemRequestService, never()).getOtherUsersRequests(anyLong(), anyInt(), anyInt());

    }

    /**
     * test getById method
     * GET-request "/requests/{requestId}"
     * should return status ok
     * should invoke service getRequestById method and return result
     */
    @SneakyThrows
    @Test
    void getById_StatusIsOk_InvokeService() {

        User user = User.builder().name("Olga").build();
        ItemDto item = ItemDto.builder().name("bike").build();
        List<ItemDto> items = List.of(item);
        ItemRequestDto validItemRequest = ItemRequestDto.builder()
                .description("I would like rent bike").build();
        ItemRequestOutDto itemRequest = ItemRequestMapper.toItemRequestOutDto(
                ItemRequestMapper.toItemRequest(validItemRequest, user), items).toBuilder().id(itemRequestId).build();
        String itemRequestString = objectMapper.writeValueAsString(itemRequest);

        when(itemRequestService.getRequestById(userId, requestId)).thenReturn(itemRequest);

        String result = mockMvc.perform(get("/requests/{requestId}", itemRequestId)
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonType))
                .andExpect(content().json(itemRequestString))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemRequestService).getRequestById(userId, requestId);
        assertEquals(result, itemRequestString);

    }


}
