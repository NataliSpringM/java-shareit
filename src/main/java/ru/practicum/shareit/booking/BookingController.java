package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.groups.Create;
import ru.practicum.shareit.util.groups.Update;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Sprint add-bookings.
 * processing HTTP-requests to "/bookings" end-point to add, update or get bookings' data.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    /**
     * processing POST-request to add booking (save and assign identity)
     *
     * @param userId     owner's id
     * @param bookingDto booking to save and register
     * @return booking request with assigned id
     */
    @PostMapping()
    @Validated({Create.class})
    public BookingOutDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @Valid @RequestBody BookingDto bookingDto) {
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
    @GetMapping("{bookingId}")
    public BookingOutDto getById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {
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
    @PatchMapping("{bookingId}")
    @Validated({Update.class})
    public BookingOutDto updateStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable("bookingId") Long bookingId,
                                      @RequestParam("approved") Boolean approved) {
        log.info("PATCH-request: подтверждение бронирования c id: {}, владелец {}, подтверждено: {}",
                bookingId, userId, approved);
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

    @GetMapping("owner")
    public List<BookingOutDto> getListByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(value = "state",
                                                      defaultValue = "ALL") String state,
                                              @PositiveOrZero @RequestParam(
                                                      name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(
                                                      name = "size", defaultValue = "10") Integer size) {
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
    public List<BookingOutDto> getListByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam(value = "state",
                                                       defaultValue = "ALL") String state,
                                               @PositiveOrZero @RequestParam(
                                                       name = "from", defaultValue = "0") Integer from,
                                               @Positive @RequestParam(
                                                       name = "size", defaultValue = "10") Integer size) {
        log.info("GET-запрос: список бронирований по id бронировавшего пользователя: id {}, состояние бронирования: {}"
                + ", начиная с позиции {}, ограничение размера: {}", userId, state, from, size);
        return bookingService.getListByBooker(userId, state, from, size);
    }

}
