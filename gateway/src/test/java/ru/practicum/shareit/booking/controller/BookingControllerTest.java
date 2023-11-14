package ru.practicum.shareit.booking.controller;

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
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.bookingstate.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * BookingController tests
 */

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingClient bookingClient;
    Long userId;

    /**
     * create data for tests
     */
    @BeforeEach
    @SneakyThrows
    void before() {
        userId = 1L;
    }


    /**
     * test bookItem method
     * POST-request "/bookings"
     * when booking data are valid
     * should return status ok
     * should invoke service bookItem method and return result
     */

    @Test
    @SneakyThrows
    public void bookItem_WhenBookingIsValid_StatusIsOk_AndInvokeService() {

        // create valid start and end time
        LocalDateTime futureStart = LocalDateTime.of(2024, 1, 1, 1, 1, 1);
        LocalDateTime futureEnd = LocalDateTime.of(2024, 2, 1, 1, 1, 1);
        Long itemId = 1L;

        // create valid input booking: BookingDto
        BookingDto bookingIn = BookingDto.builder()
                .itemId(itemId)
                .start(futureStart)
                .end(futureEnd)
                .build();

        // map input and out booking into strings
        String bookingInString = objectMapper.writeValueAsString(bookingIn);


        //perform tested request and check status and content
        mockMvc.perform(post(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingInString))
                .andExpect(status().isOk());

        // verify invokes
        verify(bookingClient).bookItem(eq(userId), any(BookingDto.class));

    }

    /**
     * test bookItem method
     * POST-request "/bookings"
     * when booking data are not valid: has start in past
     * should return status bad request
     * should not invoke service bookItem method
     */

    @Test
    @SneakyThrows
    public void bookItem_WhenBookingHasStartInPast_StatusIsBadRequest_DoesNotInvokeService() {

        // create invalid start time
        LocalDateTime pastStart = LocalDateTime.of(2023, 1, 1, 1, 1, 1);

        //create valid end time
        LocalDateTime futureEnd = LocalDateTime.of(2024, 2, 1, 1, 1, 1);

        // create invalid input booking: BookingDto
        BookingDto invalidBooking = BookingDto.builder()
                .start(pastStart)
                .end(futureEnd)
                .build();

        // map booking into string
        String invalidBookingString = objectMapper.writeValueAsString(invalidBooking);

        //perform tested request and check status
        mockMvc.perform(post(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBookingString))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(bookingClient, Mockito.never()).bookItem(userId, invalidBooking);
    }

    /**
     * test bookItem method
     * POST-request "/bookings"
     * when booking data are not valid: has end in past
     * should return status bad request
     * should not invoke service bookItem method
     */
    @Test
    @SneakyThrows
    public void create_WhenBookingHasEndInPast_StatusIsBadRequest_DoesNotInvokeService() {

        // create valid start time
        LocalDateTime futureStart = LocalDateTime.of(2024, 1, 1, 1, 1, 1);

        // create invalid end time
        LocalDateTime pastEnd = LocalDateTime.of(2023, 2, 1, 1, 1, 1);

        // create invalid input booking: BookingDto
        BookingDto invalidBooking = BookingDto.builder()
                .start(futureStart)
                .end(pastEnd)
                .build();

        // map booking into string
        String invalidBookingString = objectMapper.writeValueAsString(invalidBooking);

        //perform tested request and check status
        mockMvc.perform(post(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBookingString))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(bookingClient, Mockito.never()).bookItem(userId, invalidBooking);
    }

    /**
     * test bookItem method
     * POST-request "/bookings"
     * when booking data are not valid: start is null
     * should return status bad request
     * should not invoke service bookItem method
     */
    @Test
    @SneakyThrows
    public void create_WhenBookingHasStartNull_StatusIsBadRequest_DoesNotInvokeService() {

        //create valid end time
        LocalDateTime futureEnd = LocalDateTime.of(2024, 2, 1, 1, 1, 1);

        // create invalid input booking: BookingDto
        BookingDto invalidBooking = BookingDto.builder()
                .start(null)
                .end(futureEnd)
                .build();

        // map booking into string
        String invalidBookingString = objectMapper.writeValueAsString(invalidBooking);

        //perform tested request and check status
        mockMvc.perform(post(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBookingString))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(bookingClient, Mockito.never()).bookItem(userId, invalidBooking);
    }

    /**
     * test bookItem method
     * POST-request "/bookings"
     * when booking data are not valid: end is null
     * should return status bad request
     * should not invoke service bookItem method
     */
    @Test
    @SneakyThrows
    public void bookItem_WhenBookingHasEndNull_StatusIsBadRequest_DoesNotInvokeService() {

        //create valid start time
        LocalDateTime futureStart = LocalDateTime.of(2024, 2, 1, 1, 1, 1);

        // create invalid input booking: BookingDto
        BookingDto invalidBooking = BookingDto.builder()
                .start(futureStart)
                .end(null)
                .build();

        // map booking into string
        String invalidBookingString = objectMapper.writeValueAsString(invalidBooking);

        //perform tested request and check status
        mockMvc.perform(post(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBookingString))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(bookingClient, Mockito.never()).bookItem(userId, invalidBooking);
    }

    /**
     * test getBooking method
     * GET-request "/bookings/{id}"
     * should return status ok
     * should invoke service getBooking method and return result
     */

    @SneakyThrows
    @Test
    public void getBooking_StatusIsOk_InvokeService() {

        // create valid input booking: BookingDto
        Long bookingId = 1L;

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH + BOOKING_ID_PATH_VARIABLE, bookingId)
                        .header(HEADER_USER_ID, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(bookingClient).getBooking(userId, bookingId);

    }


    /**
     * test update method
     * PATCH-request "/bookings/{bookingId}"
     * when parameters are valid
     * should return status ok
     * should invoke service updateStatus method and return result
     */
    @Test
    @SneakyThrows
    public void update_StatusIsOk_AndInvokeService() {

        // create valid parameter value
        Boolean paramValue = true;

        // create valid start and end time
        LocalDateTime futureStart = LocalDateTime.of(2024, 1, 1, 1, 1, 1);
        LocalDateTime futureEnd = LocalDateTime.of(2024, 2, 1, 1, 1, 1);

        // create valid input booking: BookingDto
        Long bookingId = 1L;
        BookingDto bookingIn = BookingDto.builder()
                .start(futureStart)
                .end(futureEnd)
                .build();

        // map input and out booking into strings
        String bookingInString = objectMapper.writeValueAsString(bookingIn);

        //perform tested request and check status and content
        mockMvc.perform(patch(BOOKINGS_PATH + BOOKING_ID_PATH_VARIABLE, bookingId)
                        .header(HEADER_USER_ID, userId)
                        .param(APPROVED_PARAM_NAME, String.valueOf(paramValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingInString))
                .andExpect(status().isOk());

        // verify invokes
        verify(bookingClient).updateStatus(bookingId, userId, true);

    }

    /**
     * test update method
     * PATCH-request "/bookings/{bookingId}"
     * has required parameter "approved" which should be able to cast to Boolean value
     * when parameter has invalid name
     * should return status internal server error
     * should not invoke service updateStatus method
     */

    @Test
    @SneakyThrows
    public void update_WhenParamHasInvalidName_StatusIsInternalServerError_DoesNotInvokeService() {

        // create invalid parameter name
        String paramName = "notValidName";

        // create valid parameter value
        Boolean paramValue = true;

        // create userId
        Long userId = 1L;

        // create itemId
        Long itemId = 1L;

        // create valid input booking: BookingDto
        Long bookingId = 1L;
        BookingDto bookingIn = BookingDto.builder()
                .itemId(itemId)
                .build();

        // map input booking into string
        String bookingInString = objectMapper.writeValueAsString(bookingIn);

        //perform tested request and check status
        mockMvc.perform(patch(BOOKINGS_PATH + BOOKING_ID_PATH_VARIABLE, bookingId)
                        .header(HEADER_USER_ID, userId)
                        .param(paramName, String.valueOf(paramValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingInString))
                .andExpect(status().isInternalServerError());

        // verify invokes
        verify(bookingClient, never()).updateStatus(anyLong(), anyLong(), anyBoolean());

    }

    /**
     * test update method
     * PATCH-request "/bookings/{bookingId}"
     * has required parameter "approved" which should be cast to Boolean value
     * when parameter has invalid value: not be able to cast to Boolean
     * should return status internal server error
     * should not invoke service updateStatus method
     */
    @Test
    @SneakyThrows
    public void update_WhenParamHasInvalidValue_StatusIsInternalServerError_DoesNotInvokeService() {

        // create invalid parameter value
        String paramValue = "notValidValue";

        // create userId
        Long userId = 1L;

        // create itemId
        Long itemId = 1L;

        // create valid input booking: BookingDto
        Long bookingId = 1L;
        BookingDto bookingIn = BookingDto.builder()
                .itemId(itemId)
                .build();

        // map input booking into string
        String bookingInString = objectMapper.writeValueAsString(bookingIn);

        //perform tested request and check status
        mockMvc.perform(patch(BOOKINGS_PATH + BOOKING_ID_PATH_VARIABLE, bookingId)
                        .header(HEADER_USER_ID, userId)
                        .param(APPROVED_PARAM_NAME, paramValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingInString))
                .andExpect(status().isInternalServerError());

        // verify invokes
        verify(bookingClient, never()).updateStatus(anyLong(), anyLong(), anyBoolean());

    }


    /**
     * test getBookingsByOwner method
     * GET-request "/bookings/owner"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are valid
     * should return status ok
     * should invoke service getBookingsByOwner method
     */
    @Test
    @SneakyThrows
    public void getBookingsByOwner_WhenParametersAreValid_IsStatusOk_AndInvokeService() {

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH + OWNER_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(APPROVED_PARAM_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isOk());

        // verify invokes
        verify(bookingClient).getBookingsByOwner(userId, BookingState.valueOf(ALL_DEFAULT_VALUE),
                Integer.valueOf(ZERO_DEFAULT_VALUE),
                Integer.valueOf(TEN_DEFAULT_VALUE));

    }


    /**
     * test getBookingsByOwner method
     * GET-request "/bookings/owner"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "from" is negative number, or when casting to number
     * should return status bad request
     * should not invoke service getBookingsByOwner method
     */


    @Test
    @SneakyThrows
    public void getBookingsByOwner_WhenFromIsNegative_IsStatusBadRequest_DoesNotInvokeService() {

        //create invalid parameter
        Integer paramFromValue = -1;

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH + OWNER_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, String.valueOf(paramFromValue))
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isBadRequest());
        // verify invokes
        verify(bookingClient, never()).getBookingsByOwner(anyLong(), any(), anyInt(), anyInt());

    }

    /**
     * test getBookingsByOwner method
     * GET-request "/bookings/owner"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "size" is negative number, or when casting to number
     * should return status bad request
     * should not invoke service getBookingsByOwner method
     */

    @Test
    @SneakyThrows
    public void getBookingsByOwner_WhenSizeIsNegative_IsStatusBadRequest_DoesNotInvokeService() {

        //create invalid parameter
        Integer paramSizeValue = -1;

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH + OWNER_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(bookingClient, never()).getBookingsByOwner(anyLong(), any(), anyInt(), anyInt());

    }

    /**
     * test getBookingsByOwner method
     * GET-request "/bookings/owner"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "size" is zero number, or when casting to number
     * should return status bad request
     * should not invoke service getBookingsByOwner method
     */

    @Test
    @SneakyThrows
    public void getBookingsByOwner_WhenSizeIsZero_IsStatusBadRequest_DoesNotInvokeService() {

        // create invalid parameter
        Integer paramSizeValue = 0;

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH + OWNER_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(bookingClient, never()).getBookingsByBooker(anyLong(), any(), anyInt(), anyInt());

    }

    /**
     * test getBookingsByOwner method
     * GET-request "/bookings/owner"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid and "from" is not convertible to Number
     * should return status internal server error
     * should not invoke service getBookingsByOwner method
     */

    @Test
    @SneakyThrows
    public void getBookingsByOwner_WhenFromIsString_IsStatusInternalServerError_DoesNotInvokeService() {

        // create invalid parameter
        String paramFromValue = "from";

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH + OWNER_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, paramFromValue)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isInternalServerError());

        // verify invokes
        verify(bookingClient, never()).getBookingsByBooker(anyLong(), any(), anyInt(), anyInt());
    }


    /**
     * test getBookingsByOwner method
     * GET-request "/bookings/owner"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "size" is not convertible to Number
     * should return status internal server error
     * should not invoke service getBookingsByOwner method
     */


    @Test
    @SneakyThrows
    public void getBookingsByOwner_WhenSizeIsString_IsStatusInternalServerError_DoesNotInvokeService() {

        // create invalid parameter
        String paramSizeValue = "size";


        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH + OWNER_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, paramSizeValue))
                .andExpect(status().isInternalServerError());

        // verify invokes
        verify(bookingClient, never()).getBookingsByOwner(anyLong(), any(), anyInt(), anyInt());
    }

    /**
     * test getBookingsByOwner method
     * GET-request "/bookings"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "state" is UNSUPPORTED
     * should return status bad request
     * should not invoke service getBookingsByOwner method
     */
    @Test
    @SneakyThrows
    public void getBookingsByOwner_WhenStateIsNotValid_IsStatusBadRequest_DoesNotInvokeService() {

        // create invalid parameter
        String paramStateValue = "UNSUPPORTED";

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH + OWNER_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, paramStateValue)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(bookingClient, never()).getBookingsByOwner(anyLong(), any(), anyInt(), anyInt());
    }


    /**
     * test getBookingsByBooker method
     * GET-request "/bookings"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are valid
     * should return status ok
     * should invoke service getBookingsByBooker method and return result
     */

    @Test
    @SneakyThrows
    public void getBookingsByBooker_WhenParametersAreValid_IsStatusOk_AndInvokeService() {

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(APPROVED_PARAM_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isOk());

        // verify invokes
        verify(bookingClient).getBookingsByBooker(userId, BookingState.valueOf(ALL_DEFAULT_VALUE),
                Integer.valueOf(ZERO_DEFAULT_VALUE),
                Integer.valueOf(TEN_DEFAULT_VALUE));

    }

    /**
     * test getBookingsByBooker method
     * GET-request "/bookings"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "from" is negative number, or when casting to number
     * should return status bad request
     * should not invoke service getBookingsByBooker method
     */


    @Test
    @SneakyThrows
    public void getBookingsByBooker_WhenFromIsNegative_IsStatusBadRequest_DoesNotInvokeService() {

        // create invalid parameter
        Integer paramFromValue = -1;

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, String.valueOf(paramFromValue))
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(bookingClient, never()).getBookingsByBooker(anyLong(), any(), anyInt(), anyInt());

    }

    /**
     * test getBookingsByBooker method
     * GET-request "/bookings"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid and "size" is negative number, or when casting to number
     * should return status bad request
     * should not invoke service getBookingsByBooker method
     */


    @Test
    @SneakyThrows
    public void getBookingsByBooker_WhenSizeIsNegative_isStatusBadRequest_DoesNotInvokeService() {

        // create invalid parameter
        Integer paramSizeValue = -1;

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(bookingClient, never()).getBookingsByBooker(anyLong(), any(), anyInt(), anyInt());

    }

    /**
     * test getBookingsByBooker method
     * GET-request "/bookings"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "size" is zero, or when casting to number
     * should return status bad request
     * should not invoke service getBookingsByBooker method
     */
    @Test
    @SneakyThrows
    public void getBookingsByBooker_WhenSizeIsZero_IsStatusBadRequest_DoesNotInvokeService() {

        // create invalid parameter
        Integer paramSizeValue = 0;

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(bookingClient, never()).getBookingsByBooker(anyLong(), any(), anyInt(), anyInt());

    }

    /**
     * test getBookingsByBooker method
     * GET-request "/bookings"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "from" is not convertible to Number
     * should return status internal server error
     * should not invoke service getBookingsByBooker method
     */
    @Test
    @SneakyThrows
    public void getBookingsByBooker_WhenFromIsNotNumber_IsStatusInternalServerError_DoesNotInvokeService() {

        // create invalid parameter
        String paramFromValue = "from";

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, paramFromValue)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isInternalServerError());

        // verify invokes
        verify(bookingClient, never()).getBookingsByBooker(anyLong(), any(), anyInt(), anyInt());

    }

    /**
     * test getBookingsByBooker method
     * GET-request "/bookings"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "size" is not convertible to Number
     * should return status internal server error
     * should not invoke service getBookingsByBooker method
     */
    @Test
    @SneakyThrows
    public void getBookingsByBooker_WhenSizeIsNotNumber_IsStatusInternalServerError_DoesNotInvokeService() {

        // create invalid parameter
        String paramSizeValue = "size";


        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, ALL_DEFAULT_VALUE)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, paramSizeValue))
                .andExpect(status().isInternalServerError());

        // verify invokes
        verify(bookingClient, never()).getBookingsByBooker(anyLong(), any(), anyInt(), anyInt());
    }

    /**
     * test getBookingsByBooker method
     * GET-request "/bookings"
     * has not required parameter "state" with default value = "ALL:
     * has not required parameter, "from" with default value = "0", must be zero or positive when casting to Number
     * has not required parameter "size" with default value = "10", must be positive when casting to Number
     * parameters "from" and "size" should be convertible to Number value
     * when parameters are not valid: "state" is UNSUPPORTED
     * should return status bad request
     * should not invoke service getBookingsByBooker method
     */
    @Test
    @SneakyThrows
    public void getBookingsByBooker_WhenStateIsNotValid_IsStatusBadRequest_DoesNotInvokeService() {

        // create invalid parameter
        String paramStateValue = "UNSUPPORTED";

        //perform tested request and check status
        mockMvc.perform(get(BOOKINGS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .param(BOOKING_STATE_PARAMETER_NAME, paramStateValue)
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(bookingClient, never()).getBookingsByBooker(anyLong(), any(), anyInt(), anyInt());
    }


}
