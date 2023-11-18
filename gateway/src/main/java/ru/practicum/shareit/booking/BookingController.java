package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.bookingstate.BookingState;
import ru.practicum.shareit.util.validation.Validation;


import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.util.constants.Constants.*;
import static ru.practicum.shareit.util.constants.Constants.APPROVED_PARAM_NAME;


/**
 * processing HTTP-requests to "/bookings" end-point to add, update or get bookings' data.
 */
@Controller
@RequestMapping(path = BOOKINGS_PATH)
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    /**
     * processing POST-request to add booking
     *
     * @param userId     booker's id
     * @param bookingDto booking to save and register
     * @return booking request with assigned id
     */
    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader(HEADER_USER_ID) Long userId,
                                           @RequestBody @Valid BookingDto bookingDto) {
        Validation.checkStartIsNotEqualsOrBeforeEnd(bookingDto);
        log.info("Creating booking {}, userId={}", bookingDto, userId);
        return bookingClient.bookItem(userId, bookingDto);
    }

    /**
     * processing a GET-request to get a booking by id
     *
     * @param userId    user's id
     * @param bookingId booking's id
     * @return booking
     */
    @GetMapping(BOOKING_ID_PATH_VARIABLE)
    public ResponseEntity<Object> getBooking(@RequestHeader(HEADER_USER_ID) Long userId,
                                             @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    /**
     * processing PATCH-request to approve or decline booking
     *
     * @param userId        owner's id
     * @param bookingId     booking id
     * @param approvedParam approving or declining (boolean true or false respectively)
     * @return approved or declined booking
     */
    @PatchMapping(BOOKING_ID_PATH_VARIABLE)
    public ResponseEntity<Object> updateStatus(@RequestHeader(HEADER_USER_ID) Long userId,
                                               @PathVariable Long bookingId,
                                               @RequestParam(APPROVED_PARAM_NAME) Boolean approvedParam) {
        log.info("Resolve booking status bookingId:={}, ownerId={}, approved: {}", //TODO
                bookingId, userId, approvedParam); //TODO
        return bookingClient.updateStatus(userId, bookingId, approvedParam);

    }

    /**
     * processing a GET-request to get a booking' list for a specific owner by booking's state
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     *
     * @param userId     owner's id
     * @param stateParam booking's state (default: all bookings)
     * @return list of bookings according to specified criteria
     */
    @GetMapping(OWNER_PATH)
    public ResponseEntity<Object> getBookingsByOwner(@RequestHeader(HEADER_USER_ID) Long userId,
                                                     @RequestParam(
                                                             name = BOOKING_STATE_PARAMETER_NAME,
                                                             defaultValue = ALL_DEFAULT_VALUE) String stateParam,
                                                     @PositiveOrZero @RequestParam(
                                                             name = FROM_PARAMETER_NAME,
                                                             defaultValue = ZERO_DEFAULT_VALUE) Integer from,
                                                     @Positive @RequestParam(name = SIZE_PARAMETER_NAME,
                                                             defaultValue = TEN_DEFAULT_VALUE) Integer size) {
        BookingState state = Validation.getBookingStateIfValid(stateParam);
        log.info("Get booking with state {}, ownerId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookingsByOwner(userId, state, from, size);
    }

    /**
     * processing a GET request to get a booking' list for a specific booker by booking's state
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     *
     * @param userId     booker's id
     * @param stateParam booking's state (default: all bookings)
     * @return list of bookings
     */
    @GetMapping()
    public ResponseEntity<Object> getBookingsByBooker(@RequestHeader(HEADER_USER_ID) Long userId,
                                                      @RequestParam(
                                                              name = BOOKING_STATE_PARAMETER_NAME,
                                                              defaultValue = ALL_DEFAULT_VALUE) String stateParam,
                                                      @PositiveOrZero @RequestParam(
                                                              name = FROM_PARAMETER_NAME,
                                                              defaultValue = ZERO_DEFAULT_VALUE) Integer from,
                                                      @Positive @RequestParam(name = SIZE_PARAMETER_NAME,
                                                              defaultValue = TEN_DEFAULT_VALUE) Integer size) {
        BookingState state = Validation.getBookingStateIfValid(stateParam);
        log.info("Get booking with state {}, bookerId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookingsByBooker(userId, state, from, size);
    }

}
