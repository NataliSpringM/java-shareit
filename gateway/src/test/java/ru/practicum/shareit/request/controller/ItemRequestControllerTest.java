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
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.constants.Constants.*;

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
    private ItemRequestClient itemRequestClient;

    Long userId;
    Long requestId;

    @BeforeEach
    @SneakyThrows
    void before() {
        userId = 1L;
        requestId = 1L;
    }

    /**
     * test addRequest method
     * POST-request "/requests"
     * when request data are valid
     * should return status ok
     * should invoke service addRequest method and return result
     */
    @Test
    @SneakyThrows
    void addRequest_WhenItemRequestIsValid_StatusIsOk_AndInvokeService() {
        //create valid ItemRequestDto
        ItemRequestDto validItemRequest = ItemRequestDto.builder()
                .description("I would like rent bike").build();
        String validItemRequestString = objectMapper.writeValueAsString(validItemRequest);

        //perform tested request and check status
        mockMvc.perform(post(REQUESTS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validItemRequestString))
                .andExpect(status().isOk());

        //verify invokes
        verify(itemRequestClient).addRequest(eq(userId), any(ItemRequestDto.class));
    }

    /**
     * test addRequest method
     * POST-request "/requests"
     * when request data are not valid: description is empty
     * should return status bad request
     * should not invoke service addRequest method
     */
    @Test
    @SneakyThrows
    void addRequest_WhenItemRequestHasEmptyDescription_StatusIsBadRequest_DoesNotInvokeService() {

        //create invalid ItemRequestDto
        ItemRequestDto invalidItemRequest = ItemRequestDto.builder()
                .description("").build();

        //mapd dto into String
        String invalidItemRequestString = objectMapper.writeValueAsString(invalidItemRequest);

        //perform tested request and check status
        mockMvc.perform(post(REQUESTS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidItemRequestString))
                .andExpect(status().isBadRequest());

        //verify invokes
        verify(itemRequestClient, Mockito.never()).addRequest(anyLong(), any());
    }

    /**
     * test addRequest method
     * POST-request "/requests"
     * when request data are not valid: description is null
     * should return status bad request
     * should not invoke service addRequest method
     */


    @Test
    @SneakyThrows
    void addRequest_WhenItemRequestHasNullDescription_StatusIsBadRequest_DoesNotInvokeService() {

        //create invalid ItemRequestDto
        ItemRequestDto invalidItemRequest = ItemRequestDto.builder()
                .description(null).build();

        //map dto into String
        String invalidItemRequestString = objectMapper.writeValueAsString(invalidItemRequest);

        //perform tested request and check status
        mockMvc.perform(post(REQUESTS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidItemRequestString))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, Mockito.never()).addRequest(anyLong(), any());
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

        //perform tested request and check status
        mockMvc.perform(get(REQUESTS_PATH)
                        .header(HEADER_USER_ID, userId))
                .andExpect(status().isOk());

        //verify invokes
        verify(itemRequestClient).getOwnRequests(userId);

    }

    /**
     * test getOtherUserRequests method
     * GET-request "/requests/all"
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are valid
     * should return status ok
     * should invoke service getOtherUserRequests method
     */

    @Test
    @SneakyThrows
    void getOtherUsersRequests_WhenParametersAreValid_IsStatusOk_andInvokeService() {

        //perform tested request and check status
        mockMvc.perform(get(REQUESTS_PATH + ALL_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isOk());

        //verify invokes
        verify(itemRequestClient).getOtherUsersRequests(userId,
                Integer.valueOf(ZERO_DEFAULT_VALUE), Integer.valueOf(TEN_DEFAULT_VALUE));

    }

    /**
     * test getOtherUserRequests method
     * GET-request "/requests/all"
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "from" is negative number, or when casting to number
     * should throw exception
     * should not invoke service getOtherUserRequests method
     */

    @Test
    @SneakyThrows
    void getOtherUsersRequests_WhenFromIsNegative_IsStatusBadRequest_DoesNotInvokeService() {

        //create invalid parameter value
        Integer paramFromValue = -1;

        //perform tested request and check status
        mockMvc.perform(get(REQUESTS_PATH + ALL_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(FROM_PARAMETER_NAME, String.valueOf(paramFromValue))
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isBadRequest());

        //verify invokes
        verify(itemRequestClient, never()).getOtherUsersRequests(any(), any(), any());

    }


    /**
     * test getOtherUserRequests method
     * GET-request "/requests/all"
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "size" is negative number, or when casting to number
     * should throw exception
     * should not invoke service getOtherUserRequests method
     */

    @Test
    @SneakyThrows
    void getOtherUsersRequests_WhenSizeIsNegative_IsStatusBadRequest_DoesNotInvokeService() {

        //create invalid parameteer value
        Integer paramSizeValue = -1;

        //perform tested request and check status
        mockMvc.perform(get(REQUESTS_PATH + ALL_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        //verify invokes
        verify(itemRequestClient, never()).getOtherUsersRequests(anyLong(), anyInt(), anyInt());

    }

    /**
     * test getOtherUserRequests method
     * GET-request "/requests/all"
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "size" is zero number, or when casting to number
     * should throw exception
     * should not invoke service getOtherUserRequests method
     */

    @Test
    @SneakyThrows
    void getOtherUsersRequests_WhenSizeIsZero_IsStatusBadRequest_AndDoesNotInvokeService() {

        // create invalid parameter value
        Integer paramSizeValue = 0;

        //perform tested request and check statis
        mockMvc.perform(get(REQUESTS_PATH + ALL_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        //verify invokes
        verify(itemRequestClient, never()).getOtherUsersRequests(anyLong(), anyInt(), anyInt());

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

        //create invalid parameter name
        String paramFromValue = "from";

        //perform tested request and check status
        mockMvc.perform(get(REQUESTS_PATH + ALL_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(FROM_PARAMETER_NAME, paramFromValue)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isInternalServerError());

        //verify invokes
        verify(itemRequestClient, never()).getOtherUsersRequests(anyLong(), anyInt(), anyInt());

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

        //create invalid parameter
        String paramSizeValue = "size";

        //perform tested method and check status
        mockMvc.perform(get(REQUESTS_PATH + ALL_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(FROM_PARAMETER_NAME, TEN_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, paramSizeValue))
                .andExpect(status().isInternalServerError());

        //verify invokes
        verify(itemRequestClient, never()).getOtherUsersRequests(any(), any(), any());

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

        //perform tested method and check status
        mockMvc.perform(get(REQUESTS_PATH + REQUEST_ID_PATH_VARIABLE, requestId)
                        .header(HEADER_USER_ID, userId))
                .andExpect(status().isOk());

        //verify invokes
        verify(itemRequestClient).getRequestById(userId, requestId);
    }

}
