package ru.practicum.shareit.booking.repository;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * BookingRepository tests
 */
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingRepositoryTest {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    User owner;
    Long ownerId;
    Long bookerId;
    Long itemId;
    User booker;
    Item item;
    Booking waiting;
    Booking approved;
    Booking rejected;
    Booking current;
    Booking past;
    Pageable page;

    /**
     * create records in database to test
     */
    @BeforeEach
    public void beforeEach() {
        page = PageRequest.of(0, 10);
        owner = User.builder().name("Olga").email("Olga@yandex.ru").build();
        owner = userRepository.save(owner);
        ownerId = owner.getId();
        booker = User.builder().name("Alex").email("Alex@yandex.ru").build();
        booker = userRepository.save(booker);
        bookerId = booker.getId();
        item = Item.builder().id(1L).name("bike").description("new").available(true).owner(owner).build();
        item = itemRepository.save(item);
        itemId = item.getId();
        waiting = Booking.builder()
                .start(LocalDateTime.of(2024, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2024, 2, 1, 1, 1, 1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        approved = waiting.toBuilder()
                .start(LocalDateTime.of(2024, 1, 1, 1, 1, 2))
                .status(BookingStatus.APPROVED)
                .build();
        rejected = waiting.toBuilder()
                .start(LocalDateTime.of(2024, 1, 1, 1, 1, 0))
                .status(BookingStatus.REJECTED)
                .build();
        current = approved.toBuilder()
                .start(LocalDateTime.of(2023, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2024, 2, 1, 1, 1, 1))
                .build();
        past = approved.toBuilder()
                .start(LocalDateTime.of(2020, 2, 1, 1, 1, 1))
                .end(LocalDateTime.of(2020, 3, 1, 1, 1, 1))
                .build();
        bookingRepository.save(waiting);
        bookingRepository.save(approved);
        bookingRepository.save(rejected);
        bookingRepository.save(past);
        bookingRepository.save(current);
    }


    /**
     * should find ALL bookings by OWNER's id, sorting by start value, starting with new
     */
    @Test
    public void findAllByItem_Owner_IdOrderByStartDesc() {

        List<Booking> result = bookingRepository
                .findAllByItem_Owner_IdOrderByStartDesc(ownerId, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(5)
                .contains(waiting)
                .contains(approved)
                .contains(rejected)
                .contains(past)
                .contains(current)
                .startsWith(approved)
                .endsWith(past);
    }

    /**
     * should find PAST bookings by OWNER's id, sorting by start value, starting with new
     */
    @Test
    public void findAllByItem_Owner_IdAndEndIsBeforeOrderByStartDesc() {

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingRepository
                .findAllByItem_Owner_IdAndEndIsBeforeOrderByStartDesc(ownerId, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .doesNotContain(waiting)
                .contains(past)
                .startsWith(past)
                .endsWith(past);
    }

    /**
     * should find CURRENT bookings by OWNER's id, sorting by start value, starting with new
     */
    @Test
    public void findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingRepository
                .findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(ownerId, now, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .doesNotContain(past)
                .contains(current)
                .startsWith(current)
                .endsWith(current);
    }

    /**
     * should find FUTURE bookings by OWNER's id, sorting by start value, starting with new
     */
    @Test
    public void findAllByItem_Owner_IdAndStartIsAfterOrderByStartDesc() {

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingRepository
                .findAllByItem_Owner_IdAndStartIsAfterOrderByStartDesc(ownerId, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(3)
                .contains(waiting)
                .contains(approved)
                .contains(rejected)
                .startsWith(approved)
                .endsWith(rejected);
    }

    /**
     * should find REJECTED and CANCELLED bookings by OWNER's id, sorting by start value, starting with new
     */
    @Test
   public  void findAllByItem_Owner_IdAndStatusInOrderByStartDesc() {

        List<BookingStatus> notApprovedStatus = List.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
        List<Booking> result = bookingRepository
                .findAllByItem_Owner_IdAndStatusInOrderByStartDesc(ownerId, notApprovedStatus, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(rejected)
                .startsWith(rejected)
                .endsWith(rejected);

    }

    /**
     * should find WAITING for approving bookings by OWNER's id, sorting by start value, starting with new
     */
    @Test
   public void findAllByItem_Owner_IdAndStatusOrderByStartDesc() {
        List<Booking> result = bookingRepository
                .findAllByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(waiting)
                .doesNotContain(approved)
                .doesNotContain(rejected)
                .startsWith(waiting)
                .endsWith(waiting);

    }

    /**
     * should find ALL booking by BOOKER's id, sorting by start value, starting with new
     */
    @Test
        public   void findAllByBookerIdOrderByStartDesc() {

        List<Booking> result = bookingRepository
                .findAllByBookerIdOrderByStartDesc(bookerId, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(5)
                .contains(waiting)
                .contains(approved)
                .contains(rejected)
                .startsWith(approved)
                .endsWith(past);
    }

    /**
     * should find CURRENT bookings by BOOKER's id, sorting by start value, starting with new
     */

    @Test
    public void findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc() {

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingRepository
                .findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(bookerId, now, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(current)
                .doesNotContain(approved)
                .doesNotContain(rejected)
                .startsWith(current)
                .endsWith(current);
    }

    /**
     * should find FUTURE bookings by BOOKER's id, sorting by start value, starting with new
     */
    @Test
    public void findAllByBookerIdAndStartIsAfterOrderByStartDesc() {

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingRepository
                .findAllByBookerIdAndStartIsAfterOrderByStartDesc(bookerId, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(3)
                .contains(waiting)
                .contains(approved)
                .contains(rejected)
                .startsWith(approved)
                .endsWith(rejected);
    }

    /**
     * should find PAST bookings by booker's id, sorting by start value, starting with new
     */

    @Test
    public void findAllByBookerIdAndEndIsBeforeOrderByStartDesc() {

        LocalDateTime now = LocalDateTime.now();

        List<Booking> result = bookingRepository
                .findAllByBookerIdAndEndIsBeforeOrderByStartDesc(bookerId, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(past)
                .startsWith(past)
                .endsWith(past);
    }

    /**
     * should find REJECTED and CANCELED bookings by booker's id, sorting by start value, starting with new
     */

    @Test
    public void findAllByBookerIdAndStatusInOrderByStartDesc() {

        List<BookingStatus> notApprovedStatus = List.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
        List<Booking> result = bookingRepository
                .findAllByBookerIdAndStatusInOrderByStartDesc(bookerId, notApprovedStatus, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(rejected)
                .startsWith(rejected)
                .endsWith(rejected);
    }

    /**
     * should find WAITING for approving bookings by booker's id, sorting by start value, starting with new
     */
    @Test
    public void findAllByBookerIdAndStatusOrderByStartDesc() {

        List<Booking> result = bookingRepository
                .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(waiting)
                .startsWith(waiting)
                .endsWith(waiting);
    }

    /**
     * should find LAST relative a certain time booking of a specific item with a specific status
     */
    @Test
    public void findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc() {

        LocalDateTime now = LocalDateTime.now();

        Optional<Booking> lastBooking = bookingRepository
                .findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(itemId,
                        BookingStatus.APPROVED, now, now);

        AssertionsForClassTypes.assertThat(lastBooking).hasValueSatisfying(booking -> assertThat(booking)
                .hasFieldOrPropertyWithValue("id", 5L)
                .hasFieldOrPropertyWithValue("start", current.getStart())
                .hasFieldOrPropertyWithValue("end", current.getEnd())
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("booker", booker));
    }

    /**
     * should find NEXT relative a certain time booking of a specific item with a specific status
     */
    @Test
    public void findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart() {

        LocalDateTime now = LocalDateTime.now();

        Optional<Booking> lastBooking = bookingRepository
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(itemId,
                        BookingStatus.APPROVED, now, now);

        AssertionsForClassTypes.assertThat(lastBooking).hasValueSatisfying(booking -> assertThat(booking)
                .hasFieldOrPropertyWithValue("id", 2L)
                .hasFieldOrPropertyWithValue("start", approved.getStart())
                .hasFieldOrPropertyWithValue("end", approved.getEnd())
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("booker", booker));
    }

    /**
     * should find PAST or CURRENT bookings relative a certain time of a specific item with a specific status
     */
    @Test
    public void findAllByItem_IdAndBooker_IdAndStatusAndStartIsBefore() {

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingRepository
                .findAllByItem_IdAndBooker_IdAndStatusAndStartIsBefore(itemId, bookerId, BookingStatus.APPROVED, now);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(2)
                .startsWith(past)
                .endsWith(current);
    }

    /**
     * clear records in database
     */
    @AfterEach
    public void afterEach() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
    }
}
