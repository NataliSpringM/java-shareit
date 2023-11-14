package ru.practicum.shareit.booking.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {
    /**
     * map Booking object into BookingOutDto object
     *
     * @param booking Booking object
     * @return BookingOutDto object
     */
    public static BookingOutDto toBookingOutDto(Booking booking) {
        return new BookingOutDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                booking.getBooker(),
                booking.getItem()
        );
    }

    /**
     * map Booking object into BookingItemDto object
     *
     * @param booking Booking object
     * @return BookingItemDto object
     */
    public static BookingItemDto toBookingItemDto(Booking booking) {
        return new BookingItemDto(
                booking.getId(),
                booking.getBooker().getId()
        );
    }

    /**
     * map into Booking object
     *
     * @param bookingDto BookingDto object
     * @param user       booker
     * @param item       item
     * @param status     BookingStatus
     * @return Booking object
     */
    public static Booking toBooking(BookingDto bookingDto,
                                    User user, Item item, BookingStatus status) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .booker(user)
                .item(item)
                .status(status)
                .build();
    }

    /**
     * map into Booking object
     *
     * @param bookingOutDto BookingOutDto object
     * @return Booking object
     */
    public static Booking toBooking(BookingOutDto bookingOutDto) {
        return Booking.builder()
                .id(bookingOutDto.getId())
                .start(bookingOutDto.getStart())
                .end(bookingOutDto.getEnd())
                .booker(bookingOutDto.getBooker())
                .item(bookingOutDto.getItem())
                .status(bookingOutDto.getStatus())
                .build();
    }

    /**
     * map List of Booking objects into List of BookingOutDto objects
     *
     * @param bookings list of Booking objects
     * @return List of BookingOutDto objects
     */

    public static List<BookingOutDto> toBookingOutDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toBookingOutDto)
                .collect(Collectors.toList());
    }

    /**
     * map BookingOutDto object into BookingItemDto object to test
     *
     * @param bookingOutDto BookingOutDto object
     * @return BookingItemDto object
     */
    public static BookingItemDto toBookingItemDto(BookingOutDto bookingOutDto) {
        return new BookingItemDto(
                bookingOutDto.getId(),
                bookingOutDto.getBooker().getId()
        );
    }

}

