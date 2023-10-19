package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.groups.Create;
import ru.practicum.shareit.util.groups.Update;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    /**
     * create BookingRequestDto
     *
     * @param userId            owner's id
     * @param bookingRequestDto BookingRequestDto object to register
     * @return registered BookingRequestDto object
     */
    @PostMapping()
    @Validated({Create.class})
    public BookingResponseDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        log.info("POST-request: создание бронирования c id: {}, {}", userId, bookingRequestDto);
        return bookingService.create(userId, bookingRequestDto);
    }

    /**
     * processing a GET request to get a booking by id
     *
     * @param userId    user's id
     * @param bookingId booking's id
     * @return UserDto object
     */
    @GetMapping("{bookingId}")
    public BookingResponseDto getById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {
        log.info("Get-request: получение бронирования по id бронирования: {}, владелец вещи: {}", userId, bookingId);
        return bookingService.getById(userId, bookingId);
    }

    /**
     * processing PATCH-request to approve or decline booking
     *
     * @param userId    user's id
     * @param bookingId booking id
     * @param approved  approving or declining
     * @return updated UserResponseDto object
     */
    @PatchMapping("{bookingId}")
    @Validated({Update.class})
    public BookingResponseDto updateStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @PathVariable("bookingId") Long bookingId, @RequestParam("approved") Boolean approved) {
        log.info("PATCH-request: подтверждение бронирования c id: {}, владелец {}, подтверждено: {}",
                bookingId, userId, approved);
        return bookingService.updateStatus(bookingId, userId, approved);

    }

    /**
     * processing a GET request to get a booking' list for a specific owner by booking's state
     *
     * @param userId owner's id
     * @param state  booking's state (default: all bookings)
     * @return list of bookings according to specified criteria
     */

    @GetMapping("owner")
    public List<BookingResponseDto> getListByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(value = "state",
                                                           defaultValue = "ALL") String state) {
        log.info("GET-запрос: список бронирований по id владельца вещей: id {}, состояние бронирования: {}",
                userId, state);
        return bookingService.getListByOwner(userId, state);
    }

    /**
     * processing a GET request to get a booking' list for a specific booker by booking's state
     *
     * @param userId booker's id
     * @param state  booking's state (default: all bookings)
     * @return list of bookings
     */
    @GetMapping()
    public List<BookingResponseDto> getListByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestParam(value = "state",
                                                            defaultValue = "ALL") String state) {
        log.info("GET-запрос: список бронирований по id бронировавшего пользователя: id {}, состояние бронирования: {}",
                userId, state);
        return bookingService.getListByBooker(userId, state);
    }

}
