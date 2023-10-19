package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

/**
 * BookingService interface
 */
public interface BookingService {
    /**
     * create BookingRequestDto
     *
     * @param userId            owner's id
     * @param bookingRequestDto BookingRequestDto object to register
     * @return registered BookingRequestDto object
     */
    BookingResponseDto create(Long userId, BookingRequestDto bookingRequestDto);

    /**
     * get BookingResponseDto object by booking's id for owner or booker
     *
     * @param userId    user's id
     * @param bookingId booking's id
     * @return BookingResponseDto object
     */
    BookingResponseDto getById(Long userId, Long bookingId);

    /**
     * set APPROVE or REJECTED BookingStatus for booking. Updating of status is allowed only for item's owner
     *
     * @param bookingId booking's id
     * @param userId    user's id
     * @param approved  boolean
     * @return BookingResponseDto with updated (APPROVED or REJECTED status)
     */
    BookingResponseDto updateStatus(Long bookingId, Long userId, Boolean approved);

    /**
     * Find if exists list of booking by OWNER's id, sorting by start value, starting with new
     * by state (default = ALL)
     *
     * @param ownerId owner's id
     * @return list of bookings of items' OWNER according to specified criteria, sorting by start in descending order
     */
    List<BookingResponseDto> getListByOwner(Long ownerId, String state);

    /**
     * Find if exists list of booking by user's id, sorting by start value, starting with new
     * by state (default = ALL)
     *
     * @param bookerId user's id
     * @return list of bookings of a specific user according to specified criteria, sorting by start in descending order
     */
    List<BookingResponseDto> getListByBooker(Long bookerId, String state);
}
