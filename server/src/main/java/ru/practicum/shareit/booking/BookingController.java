package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * processing HTTP-requests to "/bookings" end-point to add, update or get bookings' data.
 */
@RestController
@RequestMapping(path = BOOKINGS_PATH)
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    /**
     * processing POST-request to add booking (save and assign identity)
     *
     * @param userId     booker's id
     * @param bookingDto booking to save and register
     * @return booking request with assigned id
     */
    @PostMapping
    public BookingOutDto create(@RequestHeader(HEADER_USER_ID) Long userId,
                                @RequestBody BookingDto bookingDto) {
        log.info("POST-request: создание бронирования c id: {}, {}", userId, bookingDto);
        return bookingService.create(userId, bookingDto);
    }

    /**
     * processing a GET-request to get a booking by id
     *
     * @param userId    user's id
     * @param bookingId booking's id
     * @return booking
     */
    @GetMapping(BOOKING_ID_PATH_VARIABLE)
    public BookingOutDto getById(@RequestHeader(HEADER_USER_ID) Long userId, @PathVariable Long bookingId) {
        log.info("Get-request: получение бронирования по id бронирования: {}, владелец вещи: {}", userId, bookingId);
        return bookingService.getById(userId, bookingId);
    }

    /**
     * processing PATCH-request to approve or decline booking
     *
     * @param userId    user's id
     * @param bookingId booking id
     * @param approved  approving or declining (boolean true or false respectively)
     * @return approved or declined booking
     */
    @PatchMapping(BOOKING_ID_PATH_VARIABLE)
    public BookingOutDto updateStatus(@RequestHeader(HEADER_USER_ID) Long userId,
                                      @PathVariable Long bookingId,
                                      @RequestParam(APPROVED_PARAM_NAME) Boolean approved) {
        log.info("PATCH-request: подтверждение бронирования c id: {}, владелец {}, подтверждено: {}",
                bookingId, userId, approved); //TODO
        return bookingService.updateStatus(bookingId, userId, approved);

    }

    /**
     * processing a GET-request to get a booking' list for a specific owner by booking's state
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     *
     * @param userId owner's id
     * @param state  booking's state (default: all bookings)
     * @return list of bookings according to specified criteria
     */

    @GetMapping(OWNER_PATH)
    public List<BookingOutDto> getListByOwner(@RequestHeader(HEADER_USER_ID) Long userId,
                                              @RequestParam(
                                                      value = BOOKING_STATE_PARAMETER_NAME,
                                                      defaultValue = ALL_DEFAULT_VALUE) BookingState state,
                                              @RequestParam(
                                                      name = FROM_PARAMETER_NAME,
                                                      defaultValue = ZERO_DEFAULT_VALUE) Integer from,
                                              @RequestParam(
                                                      name = SIZE_PARAMETER_NAME,
                                                      defaultValue = TEN_DEFAULT_VALUE) Integer size) {
        log.info("GET-запрос: список бронирований по id владельца вещей: id {}, состояние бронирования: {}"
                        + ", начиная с позиции {}, ограничение размера: {}",
                userId, state, from, size);
        return bookingService.getListByOwner(userId, state, from, size);
    }

    /**
     * processing a GET request to get a booking' list for a specific booker by booking's state
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     *
     * @param userId booker's id
     * @param state  booking's state (default: all bookings)
     * @return list of bookings
     */
    @GetMapping()
    public List<BookingOutDto> getListByBooker(@RequestHeader(HEADER_USER_ID) Long userId,
                                               @RequestParam(
                                                       value = BOOKING_STATE_PARAMETER_NAME,
                                                       defaultValue = ALL_DEFAULT_VALUE) BookingState state,
                                               @RequestParam(
                                                       name = FROM_PARAMETER_NAME,
                                                       defaultValue = ZERO_DEFAULT_VALUE) Integer from,
                                               @RequestParam(
                                                       name = SIZE_PARAMETER_NAME,
                                                       defaultValue = TEN_DEFAULT_VALUE) Integer size) {
        log.info("GET-запрос: список бронирований по id бронировавшего пользователя: id {}, состояние бронирования: {}"
                + ", начиная с позиции {}, ограничение размера: {}", userId, state, from, size);
        return bookingService.getListByBooker(userId, state, from, size);
    }

}
