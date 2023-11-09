package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
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

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    /**
     * create (save and assign identity) booking, booking is not allowed for item's owner
     * throws 400.BAD_REQUEST IncorrectTimeException if start and end time of Booking are invalid
     * throws 404.NOT FOUND ObjectNotFoundException if item is not found
     * throws 400.BAD_REQUEST UnavailableItemException if item is not available
     * throws 404.NOT FOUND ObjectNotFoundException if user is not found
     * throws 404.NOT_FOUND AccessIsNotAllowedException if owner try booking
     *
     * @param userId     owner's id
     * @param bookingDto booking to save and register
     * @return booking with assigned id
     */
    @Override
    @Transactional
    public BookingOutDto create(Long userId, BookingDto bookingDto) {

        checkValidDateAndTime(bookingDto.getStart(), bookingDto.getEnd());
        Item item = getItemByIdIfExists(bookingDto.getItemId());
        checkIsItemAvailable(item);
        User user = getUserByIdIfExists(userId);
        checkAccessForOwnerNotAllowed(item, userId);

        Booking booking = BookingMapper.toBooking(bookingDto, user, item,
                BookingStatus.WAITING);
        Booking bookingWithId = bookingRepository.save(booking);
        log.info("Произведено бронирование: {}", bookingWithId);
        return BookingMapper.toBookingOutDto(bookingWithId);
    }

    /**
     * get booking by booking's id for owner or booker
     * throws 404.NOT FOUND ObjectNotFoundException if booking is not found
     * throws 400.BAD REQUEST AccessIsNotAllowedException if user is not item's owner or booker
     *
     * @param userId    user's id
     * @param bookingId booking's id
     * @return booking
     */
    @Override
    @Transactional(readOnly = true)
    public BookingOutDto getById(Long userId, Long bookingId) {

        Booking booking = getBookingByIdIfExists(bookingId);
        checkAccessAllowedOnlyForOwnerOrBooker(booking, userId);

        BookingOutDto bookingOutDto = BookingMapper.toBookingOutDto(booking);
        log.info("Пользователю с id {} предоставлена информация о бронировании {}", userId, bookingOutDto);
        return bookingOutDto;
    }

    /**
     * set APPROVE or REJECTED BookingStatus for booking. Updating of status is allowed only for item's owner
     * throws 404.NOT FOUND ObjectNotFoundException if user is not found
     * throws 404.NOT FOUND ObjectNotFoundException if booking is not found
     * throws 404.NOT_FOUND AccessIsNotAllowedException if user is not item's owner
     * throws 400.BAD_REQUEST UnavailableItemException if status is not WAITING
     *
     * @param bookingId booking's id
     * @param userId    user's id
     * @param approved  boolean
     * @return booking with updated (APPROVED or REJECTED status)
     */
    @Override
    @Transactional
    public BookingOutDto updateStatus(Long bookingId, Long userId, Boolean approved) {

        checkUserExists(userId);
        Booking booking = getBookingByIdIfExists(bookingId);
        checkAccessAllowedOnlyForOwner(booking.getItem(), userId);
        checkStatusIsWaiting(booking);

        BookingStatus status = resolveStatus(approved);
        Booking updated = booking.toBuilder().status(status).build();
        bookingRepository.save(updated);
        BookingOutDto bookingOutDto = BookingMapper.toBookingOutDto(updated);
        log.info("Бронирование {} получило статус {}", bookingOutDto, status);
        return bookingOutDto;
    }

    /**
     * find if exists list of booking by owner's id, sorting by start value, starting with new
     * by state (default = ALL)
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     * throws 404.NOT_FOUND ObjectNotFoundException if user doesn't exist
     * throws 400.BAD_REQUEST UnsupportedStatusException if state is not BookingStatus
     *
     * @param ownerId owner's id
     * @return list of bookings of items' owner according to specified criteria, sorting by start in descending order
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookingOutDto> getListByOwner(Long ownerId, String state, Integer from, Integer size) {

        checkUserExists(ownerId);
        BookingState validState = getValidBookingStateOrElseThrow(state);
        LocalDateTime now = LocalDateTime.now();
        int page = from / size;
        Pageable pageRequest = PageRequest.of(page, size);

        List<Booking> listByOwner;

        switch (validState) {
            case ALL:
                listByOwner = bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(ownerId, pageRequest);
                break;
            case CURRENT:
                listByOwner = bookingRepository
                        .findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(ownerId, now, now,
                                pageRequest);
                break;
            case PAST:
                listByOwner = bookingRepository.findAllByItem_Owner_IdAndEndIsBeforeOrderByStartDesc(ownerId, now,
                        pageRequest);
                break;
            case FUTURE:
                listByOwner = bookingRepository.findAllByItem_Owner_IdAndStartIsAfterOrderByStartDesc(ownerId, now,
                        pageRequest);
                break;
            case REJECTED:
                List<BookingStatus> notApprovedStatus = List.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
                listByOwner = bookingRepository
                        .findAllByItem_Owner_IdAndStatusInOrderByStartDesc(ownerId, notApprovedStatus, pageRequest);
                break;
            case WAITING:
                listByOwner = bookingRepository
                        .findAllByItem_Owner_IdAndStatusOrderByStartDesc(ownerId,
                                BookingStatus.valueOf("WAITING"), pageRequest);
                break;
            default:
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }

        return BookingMapper.toBookingOutDtoList(listByOwner);
    }

    /**
     * find if exists list of booking by user's id, sorting by start value, starting with new
     * by state (default = ALL)
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     * throws 404.NOT_FOUND ObjectNotFoundException if user doesn't exist
     * throws 400.BAD_REQUEST UnsupportedStatusException if state is not BookingStatus
     *
     * @param bookerId user's id
     * @return list of bookings of a specific user according to specified criteria, sorting by start in descending order
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookingOutDto> getListByBooker(Long bookerId, String state, Integer from, Integer size) {

        checkUserExists(bookerId);
        BookingState validState = getValidBookingStateOrElseThrow(state);
        LocalDateTime now = LocalDateTime.now();
        int page = from / size;
        Pageable pageRequest = PageRequest.of(page, size);

        List<Booking> listByBooker;

        switch (validState) {
            case ALL:
                listByBooker = bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId, pageRequest);
                break;
            case CURRENT:
                listByBooker = bookingRepository
                        .findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(bookerId, now, now,
                                pageRequest);
                break;
            case PAST:
                listByBooker = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(bookerId, now,
                        pageRequest);
                break;
            case FUTURE:
                listByBooker = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(bookerId, now,
                        pageRequest);
                break;
            case REJECTED:
                List<BookingStatus> notApprovedStatus = List.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
                listByBooker = bookingRepository
                        .findAllByBookerIdAndStatusInOrderByStartDesc(bookerId, notApprovedStatus, pageRequest);
                break;
            case WAITING:
                listByBooker = bookingRepository
                        .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.valueOf("WAITING"),
                                pageRequest);
                break;
            default:
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        return BookingMapper.toBookingOutDtoList(listByBooker);

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
        return itemRepository.findById(itemId)
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
        return bookingRepository.findById(bookingId)
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
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(String.format("Пользователя с id %d не существует", userId)));
    }

    /**
     * check whether User exists throw 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param userId user's id
     */
    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
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
     * check whether user is item's owner or booker
     * throws 404.NOT_FOUND AccessIsNotAllowedException if isn't
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
     * check whether state is valid BookingState
     * throws 400.BAD_REQUEST UnsupportedStatusException if isn't
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
     * check whether booking has status WAITING
     * throws 400.BAD_REQUEST UnavailableItemException if isn't
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

