package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.bookingstate.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.util.client.BaseClient;

import java.util.Map;

import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * creation requests to "/bookings" endpoint
 */
@Service
public class BookingClient extends BaseClient {

    @Autowired
    public BookingClient(@Value(API_SERVER_URL) String serverUrl, RestTemplateBuilder restTemplateBuilder) {
        super(
                restTemplateBuilder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + BOOKINGS_PATH))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }


    /**
     * create POST-request to add booking
     *
     * @param userId     booker's id
     * @param bookingDto booking to save and register
     * @return POST-request
     */
    public ResponseEntity<Object> bookItem(Long userId, BookingDto bookingDto) {
        return post(EMPTY_PATH, userId, bookingDto);
    }

    /**
     * create GET-request to get a booking by id
     *
     * @param userId    user's id
     * @param bookingId booking's id
     * @return GET-request
     */
    public ResponseEntity<Object> getBooking(Long userId, Long bookingId) {
        return get(SLASH_PATH + bookingId, userId);
    }

    /**
     * create PATCH-request to approve or decline booking
     *
     * @param userId    user's id
     * @param bookingId booking id
     * @param approved  approving or declining (boolean true or false respectively)
     * @return PATCH-request
     */
    public ResponseEntity<Object> updateStatus(Long userId, Long bookingId, Boolean approved) {
        Map<String, Object> parameters = Map.of(
                APPROVED_PARAM_NAME, approved);
        return patch(SLASH_PATH + bookingId + constructApprovedParametersPath(approved), userId, parameters);
    }

    /**
     * create GET-request to get a booking' list for a specific owner by booking's state
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     *
     * @param userId owner's id
     * @param state  booking's state (default: all bookings)
     * @return GET-request
     */
    public ResponseEntity<Object> getBookingsByOwner(Long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                BOOKING_STATE_PARAMETER_NAME, state.name(),
                FROM_PARAMETER_NAME, from,
                SIZE_PARAMETER_NAME, size
        );
        /* return get("?state={state}&from={from}&size={size}", userId, parameters);*/
        return get(OWNER_PATH + constructPagingParametersPath(state, from, size), userId, parameters);
    }

    /**
     * create GET-request to get a booking' list for a specific booker by booking's state
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     *
     * @param userId booker's id
     * @param state  booking's state (default: all bookings)
     * @return GET-request
     */

    public ResponseEntity<Object> getBookingsByBooker(Long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                BOOKING_STATE_PARAMETER_NAME, state.name(),
                FROM_PARAMETER_NAME, from,
                SIZE_PARAMETER_NAME, size
        );

        /*return get("?state={state}&from={from}&size={size}", userId, parameters);*/
        return get(constructPagingParametersPath(state, from, size), userId, parameters);
    }

    /**
     * construct path with parameters for PATCH-request
     *
     * @param approved boolean value whether booking is approved
     * @return path with parameters as name=value pair
     */
    private String constructApprovedParametersPath(Boolean approved) {
        return "?" +
                constructParamPair(approved);
    }

    /**
     * construct path with parameters for GET-request
     *
     * @param state bookingState to request
     * @return path with list of parameters as name=value pairs
     */

    private String constructPagingParametersPath(BookingState state, Integer from, Integer size) {
        return "?"
                + constructParamPair(BOOKING_STATE_PARAMETER_NAME, state)
                + "&"
                + constructParamPair(FROM_PARAMETER_NAME, from)
                + "&"
                + constructParamPair(SIZE_PARAMETER_NAME, size);
    }

    /**
     * construct String with name={value} pair
     */
    private String constructParamPair(String name, Object value) {
        return name
                + "="
                + value;
    }

    /**
     * construct String with APPROVED={value} pair
     */
    private String constructParamPair(Boolean value) {
        return APPROVED_PARAM_NAME
                + "="
                + value;
    }

}
