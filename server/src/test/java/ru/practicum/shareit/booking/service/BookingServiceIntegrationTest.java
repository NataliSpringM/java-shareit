package ru.practicum.shareit.booking.service;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.exceptions.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * BookingService Integration tests
 */

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {

    @Autowired
    UserService userService;
    @Autowired
    ItemService itemService;
    @Autowired
    BookingService bookingService;
    @Autowired
    ItemRequestService itemRequestService;

    /**
     * test create method
     * when start and end time are valid
     * when item is found by itemId in BookingDto
     * when item is available
     * when user is found by userId
     * when user is not owner
     * then invoke save method with BookingDto and bookerId as arguments and return BookingOutDto result
     */
    @Test
    public void shouldCreateBooking() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.create(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.create(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        ItemRequestOutDto savedRequestDto = itemRequestService.create(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        //create BookingDto and bookingId
        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        //invoke tested method
        BookingOutDto result = bookingService.create(bookerId, bookingDto);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", endAfterStart)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "bike")
                .hasFieldOrPropertyWithValue("item.description", "new")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.owner", owner)
                .hasFieldOrPropertyWithValue("item.owner.id", ownerId)
                .hasFieldOrPropertyWithValue("item.owner.name", owner.getName())
                .hasFieldOrPropertyWithValue("item.owner.email", owner.getEmail())
                .hasFieldOrPropertyWithValue("item.request.id", requestId)
                .hasFieldOrPropertyWithValue("item.request.description", itemRequest.getDescription())
                .hasFieldOrPropertyWithValue("item.request.requester.id", bookerId)
                .hasFieldOrPropertyWithValue("item.request.requester.name", booker.getName())
                .hasFieldOrPropertyWithValue("item.request.requester.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", booker.getName())
                .hasFieldOrPropertyWithValue("booker.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);

    }


    /**
     * test create method
     * when start and end time are valid
     * when item is not found by itemId in BookingDto
     * then throws ObjectNotFoundException
     */
    @Test
    public void shouldFailCreateIfItemIsNotFound() {

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);

        // create Item
        Long itemId = 1L;

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.create(bookerId, bookingDto),
                String.format("Вещи с id %d не существует", itemId));

    }

    /**
     * test create method
     * when start and end time are valid
     * when item exist but not available
     * then throws UnavailableItemException
     * should not invoke user and booking repositories
     */
    @Test
    public void shouldFailCreateIfItemIsNotAvailable() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(false)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        //invoke tested method to check throws
        assertThrows(UnavailableItemException.class,
                () -> bookingService.create(bookerId, bookingDto),
                "В настоящий момент вещь недоступна для бронирования.");

    }

    /**
     * test create method
     * when start and end time are valid
     * when item is found by itemId in BookingDto
     * when item is available
     * when user is not found by userId
     * then throws ObjectNotFoundException
     */
    @Test
    public void shouldFailCreateIfUserNotFound() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create bookerId;
        Long bookerId = 2L;

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.create(bookerId, bookingDto),
                String.format("Пользователя с id %d не существует", bookerId));

    }

    /**
     * test create method
     * when start and end time are valid
     * when item is found by itemId in BookingDto
     * when item is available
     * when user is found by userId but is owner of the item
     * then throws AccessIsNotAllowedException
     */
    @Test
    public void shouldFailCreateIfUserIsOwner() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        //invoke tested method to check throws
        assertThrows(AccessIsNotAllowedException.class,
                () -> bookingService.create(ownerId, bookingDto),
                "Объект не найден среди доступных для бронирования: "
                        + "владелец не может забронировать свою вещь.");
    }

    /**
     * test get method
     * when booking is found by bookingId
     * when userId is id of the item's owner
     * should return booking
     */
    @Test
    public void shouldGetBookingByIdForOwner() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.create(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.create(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        ItemRequestOutDto savedRequestDto = itemRequestService.create(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create BookingDto and bookingId
        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto);

        //invoke tested method
        BookingOutDto result = bookingService.getById(ownerId, bookingId);

        //check result
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "bike")
                .hasFieldOrPropertyWithValue("item.description", "new")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.owner", owner)
                .hasFieldOrPropertyWithValue("item.owner.id", ownerId)
                .hasFieldOrPropertyWithValue("item.owner.name", owner.getName())
                .hasFieldOrPropertyWithValue("item.owner.email", owner.getEmail())
                .hasFieldOrPropertyWithValue("item.request.id", requestId)
                .hasFieldOrPropertyWithValue("item.request.description", itemRequest.getDescription())
                .hasFieldOrPropertyWithValue("item.request.requester.id", bookerId)
                .hasFieldOrPropertyWithValue("item.request.requester.name", booker.getName())
                .hasFieldOrPropertyWithValue("item.request.requester.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", booker.getName())
                .hasFieldOrPropertyWithValue("booker.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);

    }

    /**
     * test get method
     * when booking is found by bookingId
     * when userId is id of the item's booker
     * return booking
     */
    @Test
    public void shouldGetBookingByIdForBooker() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.create(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.create(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        ItemRequestOutDto savedRequestDto = itemRequestService.create(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create BookingDto and bookingId
        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto);

        //invoke tested method
        BookingOutDto result = bookingService.getById(bookerId, bookingId);

        //check result
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "bike")
                .hasFieldOrPropertyWithValue("item.description", "new")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.owner", owner)
                .hasFieldOrPropertyWithValue("item.owner.id", ownerId)
                .hasFieldOrPropertyWithValue("item.owner.name", owner.getName())
                .hasFieldOrPropertyWithValue("item.owner.email", owner.getEmail())
                .hasFieldOrPropertyWithValue("item.request.id", requestId)
                .hasFieldOrPropertyWithValue("item.request.description", itemRequest.getDescription())
                .hasFieldOrPropertyWithValue("item.request.requester.id", bookerId)
                .hasFieldOrPropertyWithValue("item.request.requester.name", booker.getName())
                .hasFieldOrPropertyWithValue("item.request.requester.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", booker.getName())
                .hasFieldOrPropertyWithValue("booker.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);

    }

    /**
     * test get method
     * when booking is found by bookingId
     * when userId is not the id of the item's booker or owner
     * throws AccessIsNotAllowedException
     */
    @Test
    public void shouldFailGetByIdIfUserIsNotOwnerOrBooker() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);

        // create notAllowedUserId
        Long otherId = 3L;
        UserDto otherUserDto = UserDto.builder()
                .name("Kate")
                .email("Kate@yandex.ru")
                .build();
        userService.create(otherUserDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create BookingDto and bookingId
        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto);


        //invoke tested method to check throws
        assertThrows(AccessIsNotAllowedException.class,
                () -> bookingService.getById(otherId, bookingId),
                String.format("У вас нет доступа к операции получения информации о брони с id %d. "
                        + "Доступ возможен только для инициатора брони, либо владельца вещи", bookingId));

    }

    /**
     * test updateStatus method
     * when user exists by userId
     * when booking is found by bookingId
     * when user is owner of the item
     * when bookingStatus is waiting for approving
     * when approved is true
     * return booking with status approved
     */
    @Test
    public void shouldUpdateStatusToApproved() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.create(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.create(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        ItemRequestOutDto savedRequestDto = itemRequestService.create(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create BookingDto and bookingId
        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto);

        // create expected BookingOutDto to return
        Boolean approved = true;

        //invoke tested method
        BookingOutDto result = bookingService.updateStatus(bookingId, ownerId, approved);

        //test result
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "bike")
                .hasFieldOrPropertyWithValue("item.description", "new")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.owner", owner)
                .hasFieldOrPropertyWithValue("item.owner.id", ownerId)
                .hasFieldOrPropertyWithValue("item.owner.name", owner.getName())
                .hasFieldOrPropertyWithValue("item.owner.email", owner.getEmail())
                .hasFieldOrPropertyWithValue("item.request.id", requestId)
                .hasFieldOrPropertyWithValue("item.request.description", itemRequest.getDescription())
                .hasFieldOrPropertyWithValue("item.request.requester.id", bookerId)
                .hasFieldOrPropertyWithValue("item.request.requester.name", booker.getName())
                .hasFieldOrPropertyWithValue("item.request.requester.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", booker.getName())
                .hasFieldOrPropertyWithValue("booker.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED);

    }

    /**
     * test updateStatus method
     * when user exists by userId
     * when booking is found by bookingId
     * when user is owner of the item
     * when bookingStatus is waiting for approving
     * when approved is false
     * return booking with status rejected
     */
    @Test
    public void shouldUpdateStatusToRejected() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.create(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.create(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        ItemRequestOutDto savedRequestDto = itemRequestService.create(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create BookingDto and bookingId
        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto);

        // create expected BookingOutDto to return
        Boolean approved = false;

        //invoke tested method
        BookingOutDto result = bookingService.updateStatus(bookingId, ownerId, approved);

        //test result
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "bike")
                .hasFieldOrPropertyWithValue("item.description", "new")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.owner", owner)
                .hasFieldOrPropertyWithValue("item.owner.id", ownerId)
                .hasFieldOrPropertyWithValue("item.owner.name", owner.getName())
                .hasFieldOrPropertyWithValue("item.owner.email", owner.getEmail())
                .hasFieldOrPropertyWithValue("item.request.id", requestId)
                .hasFieldOrPropertyWithValue("item.request.description", itemRequest.getDescription())
                .hasFieldOrPropertyWithValue("item.request.requester.id", bookerId)
                .hasFieldOrPropertyWithValue("item.request.requester.name", booker.getName())
                .hasFieldOrPropertyWithValue("item.request.requester.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", booker.getName())
                .hasFieldOrPropertyWithValue("booker.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("status", BookingStatus.REJECTED);

    }

    /**
     * test updateStatus method
     * when user does not exist by userId
     * then throws ObjectNotFoundException
     */
    @Test
    public void shouldFailUpdateStatusIfUserDoesNotExists() {

        //create non-existed id
        Long userId = -1L;

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);


        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create BookingDto and bookingId
        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto);

        // create approved
        Boolean approved = false;

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.updateStatus(bookingId, userId, approved),
                String.format("Пользователя с id %d не существует", userId));

    }

    /**
     * test updateStatus method
     * when user exist by userId
     * when booking is not found by bookingId
     * then throws ObjectNotFoundException
     * does not invoke any repositories more
     */
    @Test
    public void shouldFailUpdateStatusIfBookingDoesNotExist() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        //create bookingId
        Long bookingId = 1L;

        // create approved
        Boolean approved = false;

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.updateStatus(bookingId, ownerId, approved),
                String.format("Бронирования с id %d не существует", bookingId));

    }

    /**
     * test updateStatus method
     * when user exist by userId
     * when booking is found by bookingId
     * when user is not owner
     * then throws ObjectNotFoundException
     */
    @Test
    public void shouldFailUpdateStatusIfUserIsNotOwner() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.create(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.create(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        ItemRequestOutDto savedRequestDto = itemRequestService.create(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        ItemDto savedItemDto = itemService.create(ownerId, itemDto);
        Item item = ItemMapper.toItem(savedItemDto, owner, itemRequest);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create BookingDto and bookingId
        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto);

        // create approved
        Boolean approved = true;

        //invoke tested method to check throws
        assertThrows(AccessIsNotAllowedException.class,
                () -> bookingService.updateStatus(bookingId, bookerId, approved),
                String.format("Операция доступна только владельцу вещи %s :", item));

    }

    /**
     * test updateStatus method
     * when user exist by userId
     * when booking is found by bookingId
     * when user is owner
     * when bookingStatus is not Waiting
     * then throws UnavailableItemException
     */
    @Test
    public void shouldFailUpdateStatusIfItIsNotWaiting() {

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);


        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create BookingDto and bookingId
        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        BookingOutDto bookingOutDto = bookingService.create(bookerId, bookingDto);
        Booking booking = BookingMapper.toBooking(bookingOutDto);

        // create approved
        Boolean approved = true;
        bookingService.updateStatus(bookingId, ownerId, approved);

        //invoke tested method to check throws
        assertThrows(UnavailableItemException.class,
                () -> bookingService.updateStatus(bookingId, ownerId, approved),
                String.format("Вы не можете изменить ранее подтвержденный статус %s", booking.getStatus()));

    }

    /**
     * test getListByOwner
     * when user exists
     * when state is valid
     * when state is ALL
     * return resulting list of all bookings or empty list
     */
    @Test
    public void shouldGetListByOwnerAllBookings() {

        //create state
        BookingState state = BookingState.ALL;

        //create parameters of page
        int from = 0;
        int size = 10;

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);


        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create BookingDto and bookingId
        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        BookingOutDto bookingOutDto1 = bookingService.create(bookerId, bookingDto1);
        Booking booking1 = BookingMapper.toBooking(bookingOutDto1);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        BookingOutDto bookingOutDto2 = bookingService.create(bookerId, bookingDto2);
        Booking booking2 = BookingMapper.toBooking(bookingOutDto2);

        //invoke tested method
        List<BookingOutDto> result = bookingService.getListByOwner(ownerId, state, from, size);

        //check result
        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingOutDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingOutDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingOutDto(booking2)));

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);

    }

    /**
     * test getListByOwner
     * when user exists
     * when state is valid
     * when state is FUTURE
     * return resulting list of future bookings or empty list
     */
    @Test
    public void shouldGetListByOwnerFutureBookings() {

        //create state
        BookingState state = BookingState.FUTURE;

        //create parameters of page
        int from = 0;
        int size = 10;

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);


        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create BookingDto and bookingId
        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        BookingOutDto bookingOutDto1 = bookingService.create(bookerId, bookingDto1);
        Booking booking1 = BookingMapper.toBooking(bookingOutDto1);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        BookingOutDto bookingOutDto2 = bookingService.create(bookerId, bookingDto2);
        Booking booking2 = BookingMapper.toBooking(bookingOutDto2);


        //invoke tested method
        List<BookingOutDto> result = bookingService.getListByOwner(ownerId, state, from, size);

        //check result
        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingOutDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingOutDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingOutDto(booking2)));

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);

    }

    /**
     * test getListByOwner
     * when user exists
     * when state is valid
     * when state is REJECTED
     * return resulting list of bookings or empty list
     */
    @Test
    public void shouldGetListByOwnerRejectedBookings() {

        //create state
        BookingState state = BookingState.REJECTED;

        //create parameters of page
        int from = 0;
        int size = 10;

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);


        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create Bookings and update their status to Rejected
        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto1);
        bookingService.updateStatus(bookingId1, ownerId, false);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto2);
        bookingService.updateStatus(bookingId2, ownerId, false);

        //invoke tested method
        List<BookingOutDto> result = bookingService.getListByOwner(ownerId, state, from, size);

        //check result
        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingOutDto.class);

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.REJECTED);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);

    }

    /**
     * test getListByOwner
     * when user exists
     * when state is valid
     * when state is WAITING
     * return resulting list of waiting bookings or empty list
     */
    @Test
    public void shouldGetListByOwnerWaitingBookings() {

        //create state
        BookingState state = BookingState.WAITING;

        //create parameters of page
        int from = 0;
        int size = 10;

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);


        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create Bookings
        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        BookingOutDto bookingOutDto1 = bookingService.create(bookerId, bookingDto1);
        Booking booking1 = BookingMapper.toBooking(bookingOutDto1);


        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        BookingOutDto bookingOutDto2 = bookingService.create(bookerId, bookingDto2);
        Booking booking2 = BookingMapper.toBooking(bookingOutDto2);


        //invoke tested method
        List<BookingOutDto> result = bookingService.getListByOwner(ownerId, state, from, size);

        //check result

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingOutDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingOutDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingOutDto(booking2)));

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);

    }

    /**
     * test getListByOwner
     * when user does not exist
     * throws ObjectNotFoundException
     */
    @Test
    public void shouldFailGetListByUserNotFound() {

        //  create ownerId
        Long ownerId = 1L;

        //create state
        BookingState state = BookingState.WAITING;

        //create parameters of page
        int from = 0;
        int size = 10;
        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getListByOwner(ownerId, state, from, size),
                String.format("Пользователя с id %d не существует", ownerId));

    }


    /**
     * test getListByBooker
     * when user exists
     * when state is valid
     * when state is ALL
     * return resulting list of all bookings or empty list
     */
    @Test
    public void shouldGetListByBookerAllBookings() {

        //create state
        BookingState state = BookingState.ALL;

        //create parameters of page
        int from = 0;
        int size = 10;

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);


        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create Bookings
        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto1);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto2);

        //invoke tested method
        List<BookingOutDto> result = bookingService.getListByBooker(bookerId, state, from, size);

        //check result
        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingOutDto.class);

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);

    }

    /**
     * test getListByBooker
     * when user exists
     * when state is valid
     * when state is FUTURE
     * return resulting list of future bookings or empty list
     */
    @Test
    public void shouldGetListByBookerFutureBookings() {

        //create state
        BookingState state = BookingState.FUTURE;

        //create parameters of page
        int from = 0;
        int size = 10;

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);


        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create Bookings
        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        BookingOutDto bookingOutDto1 = bookingService.create(bookerId, bookingDto1);
        Booking booking1 = BookingMapper.toBooking(bookingOutDto1);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        BookingOutDto bookingOutDto2 = bookingService.create(bookerId, bookingDto2);
        Booking booking2 = BookingMapper.toBooking(bookingOutDto2);

        //invoke tested method
        List<BookingOutDto> result = bookingService.getListByBooker(bookerId, state, from, size);

        //check result
        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingOutDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingOutDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingOutDto(booking2)));

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);

    }

    /**
     * test getListByBooker
     * when user exists
     * when state is valid
     * when state is REJECTED
     * return resulting list of rejected bookings or empty list
     */
    @Test
    public void shouldGetListByBookerRejectedBookings() {

        //create state
        BookingState state = BookingState.REJECTED;

        //create parameters of page
        int from = 0;
        int size = 10;

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);


        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create Bookings and update their status to Rejected
        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto1);
        bookingService.updateStatus(bookingId1, ownerId, false);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto2);
        bookingService.updateStatus(bookingId2, ownerId, false);

        //invoke tested method
        List<BookingOutDto> result = bookingService.getListByBooker(bookerId, state, from, size);

        //check result
        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingOutDto.class);

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.REJECTED);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);

    }

    /**
     * test getListByBooker
     * when user exists
     * when state is valid
     * when state is WAITING
     * return resulting list of waiting bookings or empty list
     */
    @Test
    public void shouldGetListByBookerWaitingBookings() {

        //create state
        BookingState state = BookingState.WAITING;

        //create parameters of page
        int from = 0;
        int size = 10;

        //  create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(ownerDto);

        // create Booker
        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(bookerDto);


        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        itemRequestService.create(bookerId, itemRequestDto);

        // create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // create input BookingDto with valid start and end fields to save
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        //create Bookings
        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto1);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        bookingService.create(bookerId, bookingDto2);

        //invoke tested method
        List<BookingOutDto> result = bookingService.getListByBooker(bookerId, state, from, size);

        //check result
        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingOutDto.class);

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);

    }

    /**
     * test getListByBooker
     * when user does not exist
     * throws ObjectNotFoundException
     */
    @Test
    public void shouldFailGetListByBookerIfUserNotFound() {

        // create Booker
        Long bookerId = 1L;

        //create state
        BookingState state = BookingState.WAITING;

        //create parameters of page
        int from = 0;
        int size = 10;

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getListByBooker(bookerId, state, from, size),
                String.format("Пользователя с id %d не существует", bookerId));

    }


}
