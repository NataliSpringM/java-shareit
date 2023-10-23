package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;
import ru.practicum.shareit.util.exceptions.*;


import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Booking Service implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingJpaRepository bookingJpaRepository;
    private final ItemJpaRepository itemJpaRepository;
    private final UserJpaRepository userJpaRepository;

    /**
     * create (save and assign identity) booking, booking is not allowed for item's owner
     * throws 400.BAD_REQUEST IncorrectTimeException if start and end time of Booking are invalid
     * throws 404.NOT FOUND ObjectNotFoundException if item is not found
     * throws 400.BAD_REQUEST UnavailableItemException if item is not available
     * throws 404.NOT FOUND ObjectNotFoundException if user is not found
     * throws 404.NOT_FOUND AccessIsNotAllowedException if owner try booking
     *
     * @param userId            owner's id
     * @param bookingRequestDto booking to save and register
     * @return booking with assigned id
     */
    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingRequestDto bookingRequestDto) {

        checkValidDateAndTime(bookingRequestDto.getStart(), bookingRequestDto.getEnd());
        Item item = getItemByIdIfExists(bookingRequestDto.getItemId());
        checkIsItemAvailable(item);
        User owner = getUserByIdIfExists(userId);
        checkAccessForOwnerNotAllowed(item, userId);

        Booking booking = BookingMapper.toBooking(bookingRequestDto, owner, item,
                BookingStatus.WAITING);
        Booking bookingWithId = bookingJpaRepository.save(booking);
        log.info("Произведено бронирование: {}", bookingWithId);
        return BookingMapper.toBookingResponseDto(bookingWithId);
    }

    /**
     * get booking by booking's id for owner or booker
     * throws 404.NOT FOUND ObjectNotFoundException if booking is not found
     * throws 400.NOT_FOUND AccessIsNotAllowedException if user is not item's owner or booker
     *
     * @param userId    user's id
     * @param bookingId booking's id
     * @return booking
     */
    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getById(Long userId, Long bookingId) {

        Booking booking = getBookingByIdIfExists(bookingId);
        checkAccessAllowedOnlyForOwnerOrBooker(booking, userId);

        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(booking);
        log.info("Пользователю с id {} предоставлена информация о бронировании {}", userId, bookingResponseDto);
        return bookingResponseDto;
    }

    /**
     * set APPROVE or REJECTED BookingStatus for booking. Updating of status is allowed only for item's owner
     * throws
     *
     * @param bookingId booking's id
     * @param userId    user's id
     * @param approved  boolean
     * @return booking with updated (APPROVED or REJECTED status)
     */
    @Override
    @Transactional
    public BookingResponseDto updateStatus(Long bookingId, Long userId, Boolean approved) {

        checkUserExists(userId);
        Booking booking = getBookingByIdIfExists(bookingId);
        checkAccessAllowedOnlyForOwner(booking.getItem(), userId);
        checkStatusIsWaiting(booking);

        BookingStatus status = resolveStatus(approved);
        Booking updated = booking.toBuilder().status(status).build();
        bookingJpaRepository.save(updated);
        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(updated);
        log.info("Бронирование {} получило статус {}", bookingResponseDto, status);
        return bookingResponseDto;
    }

    /**
     * find if exists list of booking by owner's id, sorting by start value, starting with new
     * by state (default = ALL)
     * throws 404.NOT_FOUND ObjectNotFoundException if user doesn't exist
     * throws 400.BAD_REQUEST UnsupportedStatusException if state is not BookingStatus
     *
     * @param ownerId owner's id
     * @return list of bookings of items' owner according to specified criteria, sorting by start in descending order
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getListByOwner(Long ownerId, String state) {

        checkUserExists(ownerId);
        BookingState validState = getValidBookingStateOrElseThrow(state);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> listByOwner;

        switch (validState) {
            case ALL:
                listByOwner = bookingJpaRepository.findAllByItem_Owner_IdOrderByStartDesc(ownerId);
                break;
            case CURRENT:
                listByOwner = bookingJpaRepository
                        .findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(ownerId, now, now);
                break;
            case PAST:
                listByOwner = bookingJpaRepository.findAllByItem_Owner_IdAndEndIsBeforeOrderByStartDesc(ownerId, now);
                break;
            case FUTURE:
                listByOwner = bookingJpaRepository.findAllByItem_Owner_IdAndStartIsAfterOrderByStartDesc(ownerId, now);
                break;
            case REJECTED:
                List<BookingStatus> notApprovedStatus = List.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
                listByOwner = bookingJpaRepository
                        .findAllByItem_Owner_IdAndStatusInOrderByStartDesc(ownerId, notApprovedStatus);
                break;
            case WAITING:
                listByOwner = bookingJpaRepository
                        .findAllByItem_Owner_IdAndStatusOrderByStartDesc(ownerId,
                                BookingStatus.valueOf("WAITING"));
                break;
            default:
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }

        return BookingMapper.toBookingResponseDtoList(listByOwner);
    }

    /**
     * find if exists list of booking by user's id, sorting by start value, starting with new
     * by state (default = ALL)
     * throws 404.NOT_FOUND ObjectNotFoundException if user doesn't exist
     * throws 400.BAD_REQUEST UnsupportedStatusException if state is not BookingStatus
     *
     * @param bookerId user's id
     * @return list of bookings of a specific user according to specified criteria, sorting by start in descending order
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getListByBooker(Long bookerId, String state) {

        checkUserExists(bookerId);
        BookingState validState = getValidBookingStateOrElseThrow(state);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> listByBooker;

        switch (validState) {
            case ALL:
                listByBooker = bookingJpaRepository.findAllByBookerIdOrderByStartDesc(bookerId);
                break;
            case CURRENT:
                listByBooker = bookingJpaRepository
                        .findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(bookerId, now, now);
                break;
            case PAST:
                listByBooker = bookingJpaRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(bookerId, now);
                break;
            case FUTURE:
                listByBooker = bookingJpaRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(bookerId, now);
                break;
            case REJECTED:
                List<BookingStatus> notApprovedStatus = List.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
                listByBooker = bookingJpaRepository
                        .findAllByBookerIdAndStatusInOrderByStartDesc(bookerId, notApprovedStatus);
                break;
            case WAITING:
                listByBooker = bookingJpaRepository
                        .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.valueOf("WAITING"));
                break;
            default:
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        return BookingMapper.toBookingResponseDtoList(listByBooker);

    }

    /**
     * resolve BookingStatus according to boolean approved argument value: APPROVED (true), REJECTED (false)
     *
     * @param approved boolean approved status
     * @return BookingStatus
     */

    private BookingStatus resolveStatus(Boolean approved) {
        if (approved.equals(true)) {
            return BookingStatus.APPROVED;
        }
        return BookingStatus.REJECTED;
    }

    /**
     * check whether start and end time of Booking are valid throws 400.BAD_REQUEST IncorrectTimeException if aren't
     * start time should not equal end time, end time should not be before start time
     *
     * @param start booking's start time
     * @param end   booking's end time
     */
    private void checkValidDateAndTime(LocalDateTime start, LocalDateTime end) {
        if (start.equals(end) || start.isAfter(end)) {
            throw new IncorrectTimeException("Указаны некорректные даты начала и/или конца бронирования");
        }
    }

    /**
     * get Item if exists throw 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param itemId item's id
     * @return Item object
     */
    private Item getItemByIdIfExists(Long itemId) {
        return itemJpaRepository.findById(itemId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(String.format("Вещи с id %d не существует", itemId)));
    }

    /**
     * get Booking if exists throw 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param bookingId booking's id
     * @return Booking object
     */
    private Booking getBookingByIdIfExists(Long bookingId) {
        return bookingJpaRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(String.format("Бронирования с id %d не существует", bookingId)));
    }

    /**
     * get User if exists throw 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param userId user's id
     * @return User object
     */
    private User getUserByIdIfExists(Long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(String.format("Пользователя с id %d не существует", userId)));
    }

    /**
     * check whether User exists throw 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param userId user's id
     */
    private void checkUserExists(Long userId) {
        if (!userJpaRepository.existsById(userId)) {
            throw new ObjectNotFoundException(String.format("Пользователя с id %d не существует", userId));
        }
    }

    /**
     * check whether user is not item's owner, throws 404.NOT_FOUND AccessIsNotAllowedException if is
     *
     * @param item   Item item
     * @param userId user's id
     */
    private void checkAccessForOwnerNotAllowed(Item item, Long userId) {
        if (isOwner(item, userId)) {
            throw new AccessIsNotAllowedException(
                    "Объект не найден среди доступных для бронирования: владелец не может забронировать свою вещь.");
        }
    }

    /**
     * check whether user is item's owner, throws 404.NOT_FOUND AccessIsNotAllowedException if isn't
     *
     * @param item   Item item
     * @param userId user's id
     */
    private void checkAccessAllowedOnlyForOwner(Item item, Long userId) {

        if (!isOwner(item, userId)) {
            throw new AccessIsNotAllowedException(
                    (String.format("Операция доступна только владельцу вещи %s :", item)));
        }
    }

    /**
     * check whether user is item's owner or booker, throws 400.NOT_FOUND AccessIsNotAllowedException if isn't
     *
     * @param booking Booking booking
     * @param userId  user's id
     */
    private void checkAccessAllowedOnlyForOwnerOrBooker(Booking booking, Long userId) {

        if (!isOwner(booking.getItem(), userId) && !isBooker(booking, userId)) {
            throw new AccessIsNotAllowedException(
                    (String.format("У вас нет доступа к операции получения информации о брони с id %d. "
                            + "Доступ возможен только для инициатора брони, либо владельца вещи", booking.getId())));
        }
    }

    /**
     * check whether state is valid BookingState, throws 400.BAD_REQUEST UnsupportedStatusException if isn't
     *
     * @param state String state
     */
    private BookingState getValidBookingStateOrElseThrow(String state) {

        BookingState validState;
        try {
            validState = BookingState.valueOf(state);
        } catch (IllegalArgumentException exception) {
            throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        return validState;
    }

    /**
     * check whether item has status available, throws 400.BAD_REQUEST UnavailableItemException if isn't
     *
     * @param item Item item
     */
    private void checkIsItemAvailable(Item item) {
        if (!item.getAvailable()) {
            throw new UnavailableItemException("В настоящий момент вещь недоступна для бронирования.");
        }
    }

    /**
     * check whether booking has status WAITING, throws 400.BAD_REQUEST UnavailableItemException if isn't
     *
     * @param booking Booking booking
     */
    private void checkStatusIsWaiting(Booking booking) {
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new UnavailableItemException(String.format("Вы не можете изменить ранее подтвержденный статус %s",
                    booking.getStatus()));
        }
    }

    /**
     * check whether user is item's owner
     *
     * @param item   Item item
     * @param userId user's id
     * @return boolean
     */

    private boolean isOwner(Item item, Long userId) {
        return item.getOwner().getId().equals(userId);
    }

    /**
     * check whether user is booker
     *
     * @param booking Booking booking
     * @param userId  user's id
     * @return boolean
     */
    private boolean isBooker(Booking booking, Long userId) {
        return booking.getBooker().getId().equals(userId);
    }
}

