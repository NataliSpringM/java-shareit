package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Booking repository
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find if exists ALL booking by OWNER's id, sorting by start value, starting with new
     * (ALL BookingState)
     *
     * @param ownerId owner's id
     * @param page    page's parameters
     * @return list of ALL bookings of a specific OWNER, sorting by start in descending order
     */
    List<Booking> findAllByItem_Owner_IdOrderByStartDesc(Long ownerId, Pageable page);

    /**
     * Find if exists PAST bookings by OWNER's id, sorting by start value, starting with new
     * (PAST BookingState)
     *
     * @param ownerId owner's id
     * @param page    page's parameters
     * @return list of PAST bookings of a specific BOOKER, sorting by start in descending order or empty list
     */
    List<Booking> findAllByItem_Owner_IdAndEndIsBeforeOrderByStartDesc(Long ownerId,
                                                                       LocalDateTime now,
                                                                       Pageable page);


    /**
     * Find if exists CURRENT bookings by OWNER's id, sorting by start value, starting with new
     * (CURRENT BookingState)
     *
     * @param ownerId owner's id
     * @param page    page's parameters
     * @return list of CURRENT bookings of a specific OWNER, sorting by start in descending order or empty list
     */
    List<Booking> findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long ownerId,
                                                                                      LocalDateTime now,
                                                                                      LocalDateTime now1,
                                                                                      Pageable page);

    /**
     * Find if exists FUTURE bookings by OWNER's id, sorting by start value, starting with new
     * (FUTURE BookingState)
     *
     * @param ownerId owner's id
     * @param page    page's parameters
     * @return list of FUTURE bookings of a specific OWNER, sorting by start in descending order or empty list
     */
    List<Booking> findAllByItem_Owner_IdAndStartIsAfterOrderByStartDesc(Long ownerId,
                                                                        LocalDateTime now,
                                                                        Pageable page);

    /**
     * Find if exists REJECTED and CANCELLED bookings by OWNER's id, sorting by start value, starting with new
     * (REJECTED BookingState)
     *
     * @param ownerId owner's id
     * @param page    page's parameters
     * @return list of REJECTED bookings of a specific OWNER, sorting by start in descending order or empty list
     */
    List<Booking> findAllByItem_Owner_IdAndStatusInOrderByStartDesc(Long ownerId,
                                                                    List<BookingStatus> notApprovedStatus,
                                                                    Pageable page);

    /**
     * Find if exists WAITING for approving bookings by OWNER's id, sorting by start value, starting with new
     * (WAITING BookingState)
     *
     * @param ownerId owner's id
     * @param page    page's parameters
     * @return list of WAITING for approving bookings of a specific OWNER, sorting by start in descending order
     * or empty list
     */
    List<Booking> findAllByItem_Owner_IdAndStatusOrderByStartDesc(Long ownerId,
                                                                  BookingStatus waiting,
                                                                  Pageable page);

    /**
     * Find if exists all booking by BOOKER's id, sorting by start value, starting with new
     * (ALL BookingState)
     *
     * @param bookerId booker's id
     * @param page     page's parameters
     * @return list of bookings of a specific booker, sorting by start in descending order or empty list
     */
    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId,
                                                    Pageable page);

    /**
     * Find if exists current bookings by BOOKER's id, sorting by start value, starting with new
     * (CURRENT BookingState)
     *
     * @param bookerId booker's id
     * @param page     page's parameters
     * @return list of bookings of a specific booker, sorting by start in descending order or empty list
     */
    List<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long bookerId,
                                                                                 LocalDateTime now,
                                                                                 LocalDateTime now1,
                                                                                 Pageable page);

    /**
     * Find if exists FUTURE bookings by BOOKER's id, sorting by start value, starting with new
     * (FUTURE BookingState)
     *
     * @param bookerId booker's id
     * @param page     page's parameters
     * @return list of bookings of a specific booker, sorting by start in descending order or empty list
     */
    List<Booking> findAllByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId,
                                                                   LocalDateTime now,
                                                                   Pageable page);

    /**
     * Find if exists past bookings by booker's id, sorting by start value, starting with new
     * (PAST BookingState)
     *
     * @param bookerId booker's id
     * @param page     page's parameters
     * @return list of bookings of a specific booker, sorting by start in descending order or empty list
     */
    List<Booking> findAllByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId,
                                                                  LocalDateTime now,
                                                                  Pageable page);

    /**
     * Find if exists rejected and cancelled bookings by booker's id, sorting by start value, starting with new
     * (REJECTED BookingState)
     *
     * @param bookerId          booker's id
     * @param page              page's parameters
     * @param notApprovedStatus list of REJECTED and CANCELED status
     * @return list of bookings of a specific booker, sorting by start in descending order or empty list
     */
    List<Booking> findAllByBookerIdAndStatusInOrderByStartDesc(Long bookerId,
                                                               List<BookingStatus> notApprovedStatus,
                                                               Pageable page);

    /**
     * Find if exists waiting for approving bookings by booker's id, sorting by start value, starting with new
     * (WAITING BookingState)
     *
     * @param bookerId booker's id
     * @param page     page's parameters
     * @param waiting  WAITING bookingStatus
     * @return list of bookings of a specific booker, sorting by start in descending order or empty list
     */
    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId,
                                                             BookingStatus waiting,
                                                             Pageable page);

    /**
     * Find if exists LAST relative a certain time booking of a specific item with a specific status
     *
     * @param itemId item's id
     * @param status BookingStatus status
     * @param now1   time
     * @param now    time
     * @return Optional Booking object
     */
    Optional<Booking> findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(Long itemId,
                                                                                            BookingStatus status,
                                                                                            LocalDateTime now1,
                                                                                            LocalDateTime now);

    /**
     * Find if exists NEXT relative a certain time booking of a specific item with a specific status
     *
     * @param itemId item's id
     * @param status BookingStatus status
     * @param now1   time
     * @param now    time
     * @return Optional Booking object
     */
    Optional<Booking> findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(Long itemId,
                                                                                         BookingStatus status,
                                                                                         LocalDateTime now,
                                                                                         LocalDateTime now1);

    /**
     * Find if exist PAST or CURRENT bookings relative a certain time of a specific item with a specific status
     *
     * @param itemId   item's id
     * @param bookerId booker's id
     * @param approved BookingStatus
     * @param now      time
     * @return list of PAST or CURRENT APPROVED bookings of a specific item for booker or empty list
     */
    List<Booking> findAllByItem_IdAndBooker_IdAndStatusAndStartIsBefore(Long itemId,
                                                                        Long bookerId,
                                                                        BookingStatus approved,
                                                                        LocalDateTime now);

}
