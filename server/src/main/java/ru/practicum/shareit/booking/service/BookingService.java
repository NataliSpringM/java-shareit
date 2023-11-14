package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

/**
 * BookingService interface
 */
@Component
public interface BookingService {
    /**
     * to add booking (save and assign identity)
     *
     * @param userId     owner's id
     * @param bookingDto booking to save and register
     * @return booking with assigned id
     */
    BookingOutDto create(Long userId, BookingDto bookingDto);

    /**
     * get booking by booking's id for owner or booker
     *
     * @param userId    user's id
     * @param bookingId booking's id
     * @return booking
     */
    BookingOutDto getById(Long userId, Long bookingId);

    /**
     * set APPROVE or REJECTED BookingStatus for booking. Updating of status is allowed only for item's owner
     *
     * @param bookingId booking's id
     * @param userId    user's id
     * @param approved  boolean
     * @return booking with updated (APPROVED or REJECTED status)
     */
    BookingOutDto updateStatus(Long bookingId, Long userId, Boolean approved);

    /**
     * find if exists list of booking by owner's id, sorting by start value, starting with new
     * by state (default = ALL)
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     *
     * @param ownerId owner's id
     * @return list of bookings of items' owner according to specified criteria, sorting by start in descending order
     */
    List<BookingOutDto> getListByOwner(Long ownerId, BookingState state, Integer from, Integer size);

    /**
     * find if exists list of booking by user's id, sorting by start value, starting with new
     * by state (default = ALL)
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     *
     * @param bookerId user's id
     * @return list of bookings of a specific user according to specified criteria, sorting by start in descending order
     */
    List<BookingOutDto> getListByBooker(Long bookerId, BookingState state, Integer from, Integer size);
}
