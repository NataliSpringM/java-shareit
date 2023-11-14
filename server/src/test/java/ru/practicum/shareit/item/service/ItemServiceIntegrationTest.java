package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.exceptions.AccessIsNotAllowedException;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.util.exceptions.UnavailableItemException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ItemService Integration tests
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemServiceIntegrationTest {
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
     * get user when user exists
     * get itemRequest when itemRequest exist by requestId
     * should crete Item and return result
     */
    @Test
    public void shouldCreateItemWithRequest() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create notOwner
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(requesterDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        itemRequestService.create(requesterId, itemRequestDto);

        //create ItemDto
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();

        //invoke tested method
        ItemDto result = itemService.create(ownerId, itemDto);

        //check result
        assertEquals(result.getId(), itemId);
        assertEquals(result.getAvailable(), true);
        assertEquals(result.getName(), "bike");
        assertEquals(result.getDescription(), "new");
        assertEquals(result.getRequestId(), requestId);
    }

    /**
     * test create method
     * get user when user exists
     * get itemRequest when itemRequest exist by requestId
     * should create item without request
     */
    @Test
    public void shouldCreateItemWithoutRequest() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.create(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        //create ItemDto
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //create item
        Item item = ItemMapper.toItem(itemDto, owner, null);
        ItemDto itemDtoWithoutRequest = ItemMapper.toItemDto(item);
        ItemDto expectedItemDto = itemDtoWithoutRequest.toBuilder().id(itemId).build();

        //invoke tested method
        ItemDto result = itemService.create(ownerId, itemDtoWithoutRequest);

        //check result
        assertEquals(result, expectedItemDto);
        assertEquals(result.getAvailable(), true);
        assertEquals(result.getName(), "bike");
        assertEquals(result.getDescription(), "new");
        assertNull(result.getRequestId());
    }

    /**
     * test create method
     * when user does not exist throw ObjectNotFoundException
     */

    @Test
    public void shouldFailCreateIfUserNotFound() {

        //create ownerId
        Long ownerId = 1L;

        //create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //invoke tested method check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.create(ownerId, itemDto),
                String.format("Пользователя с id %d не существует", ownerId));

    }

    /**
     * test create method
     * get user when user exists
     * when requestId is not null and request by requestId not found throw ObjectNotFoundException
     */

    @Test
    public void shouldFailCreateIfRequestIsNotFoundByRequestId() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create requestId
        Long requestId = 1L;

        // create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .requestId(requestId)
                .available(true)
                .build();

        //invoke tested method check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.create(ownerId, itemDto),
                String.format("Запроса id %d не существует", requestId));
    }

    /**
     * test getById method
     * when item exists
     * when user is not owner set null lastBooking and nextBooking info
     * get comments when they exist
     * construct object ItemOutDto with comments and return result
     */
    @Test
    public void shouldGetItemByIdWithoutBookingsForNotOwner() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create notOwner
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(requesterDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        itemRequestService.create(requesterId, itemRequestDto);

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        //invoke tested method
        ItemOutDto result = itemService.getById(ownerId, itemId);

        //check result
        assertEquals(result.getAvailable(), true);
        assertEquals(result.getName(), "bike");
        assertEquals(result.getDescription(), "new");
        assertEquals(result.getRequestId(), requestId);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertEquals(result.getComments(), Collections.emptyList());
    }

    /**
     * test getById method
     * when item exists
     * when user is owner get lastBooking and nextBooking when exist
     * get comments when exist
     * construct object ItemOutDto and return result
     */
    @Test
    public void shouldGetItemByIdWithBookingsForOwner() {

        //create owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create requester
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(requesterDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        itemRequestService.create(requesterId, itemRequestDto);

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        //create nextBooking for Item
        Long nextBookingId = 1L;
        BookingDto nextBookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.of(2030, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .build();
        BookingOutDto nextBookingOutDto = bookingService.create(requesterId, nextBookingDto);
        bookingService.updateStatus(nextBookingId, ownerId, true);
        Booking nextBooking = BookingMapper.toBooking(nextBookingOutDto);
        BookingItemDto nextBookingItemDto = BookingMapper.toBookingItemDto(nextBooking);

        //invoke tested method check throws
        ItemOutDto result = itemService.getById(ownerId, itemId);

        //check result
        assertThat(result).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "bike")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("lastBooking", null)
                .hasFieldOrPropertyWithValue("nextBooking", nextBookingItemDto)
                .hasFieldOrPropertyWithValue("comments", Collections.emptyList());
    }

    /**
     * test getById method
     * when item exists
     * when user is owner
     * when bookings do not exist
     * get comments when exist
     * construct object ItemOutDto and return result
     */
    @Test
    public void shouldGetByIdForOwnerWithoutBookings() {

        //create owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create requester
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(requesterDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        itemRequestService.create(requesterId, itemRequestDto);

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        itemService.create(ownerId, itemDto);

        //invoke tested method check throws
        ItemOutDto result = itemService.getById(ownerId, itemId);

        //check result
        assertThat(result).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "bike")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("lastBooking", null)
                .hasFieldOrPropertyWithValue("nextBooking", null)
                .hasFieldOrPropertyWithValue("comments", Collections.emptyList());
    }

    /**
     * test getById method
     * when item
     * when user is not owner set null lastBooking and nextBooking info
     * get comments when they exist
     * construct object ItemOutDto with comments and return result
     */
    @Test
    public void shouldThrowExceptionIfItemNotFoundById() {

        //create ownerId
        Long ownerId = 1L;

        //create itemId
        Long itemId = 1L;

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.getById(ownerId, itemId),
                String.format("Запроса с id %d не существует", itemId));
    }


    /**
     * test update method
     * when user exists
     * when item exists
     * when user is item's owner
     * when all fields are in presence in itemDto to update and valid
     * should ignore id field
     * should return updated itemDto object
     */

    @Test
    public void shouldUpdateAllFieldsExcludingId() {

        //create ItemDto to update with valid fields
        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(44L)
                .name("NewName")
                .description("NewDescription")
                .available(false)
                .build();

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create requester
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(requesterDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        itemRequestService.create(requesterId, itemRequestDto);

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        //invoke tested method
        ItemDto result = itemService.update(ownerId, itemDtoToUpdate, itemId);

        //check result
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("description", "NewDescription")
                .hasFieldOrPropertyWithValue("available", false)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    /**
     * test update method
     * when user exists
     * when item exists
     * when user is item's owner
     * when only name field in itemDto to update and valid
     * should ignore id field
     * should ignore null fields
     * should return itemDto object with updated name
     */
    @Test
    public void shouldUpdateOnlyName() {

        //create ItemDto to update with only valid name
        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(44L)
                .name("NewName")
                .build();

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create requester
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(requesterDto);


        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        itemRequestService.create(requesterId, itemRequestDto);

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        //invoke tested method
        ItemDto result = itemService.update(ownerId, itemDtoToUpdate, itemId);

        //check result
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("requestId", requestId);

    }

    /**
     * test update method
     * when user exists
     * when item exists
     * when user is item's owner
     * when only description field in itemDto to update and valid
     * should ignore null fields
     * should return itemDto object with updated description
     */
    @Test
    public void shouldUpdateOnlyDescription() {

        //create ItemDto to update with only valid description
        ItemDto itemDtoToUpdate = ItemDto.builder()
                .description("newDescription")
                .build();

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create requester
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(requesterDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        itemRequestService.create(requesterId, itemRequestDto);

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        // invoke tested method
        ItemDto result = itemService.update(ownerId, itemDtoToUpdate, itemId);

        //check result
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "bike")
                .hasFieldOrPropertyWithValue("description", "newDescription")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    /**
     * test update method
     * when user exists
     * when item exists
     * when user is item's owner
     * when only available field in itemDto to update and valid
     * should ignore id field
     * should ignore null fields
     * should return itemDto object with updated available status
     */
    @Test
    public void shouldUpdateOnlyAvailable() {

        //create ItemDto to update with only valid available
        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(44L)
                .available(false)
                .build();

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create requester
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(requesterDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        itemRequestService.create(requesterId, itemRequestDto);

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.create(ownerId, itemDto);

        //invoke tested method
        ItemDto result = itemService.update(ownerId, itemDtoToUpdate, itemId);

        //check result
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "bike")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", false)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    /**
     * test update method
     * when user exists
     * when item exists
     * when user is item's owner
     * when all fields to update are null or invalid
     * should return nonUpdated ItemDto object
     */
    @Test
    public void shouldReturnNonUpdatedItem() {

        //create ItemDto with invalid fields to update
        ItemDto invalidItemDtoToUpdate = ItemDto.builder()
                .name("")
                .description("")
                .available(null)
                .build();

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create requester
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(requesterDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        itemRequestService.create(requesterId, itemRequestDto);

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        itemService.create(ownerId, itemDto);

        //invoke tested method
        ItemDto result = itemService.update(ownerId, invalidItemDtoToUpdate, itemId);

        //check result
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "bike")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("requestId", null);

    }

    /**
     * test update method
     * when user does not exist throw ObjectNotFoundException
     */
    @Test
    public void shouldFailUpdateIfUserDoesNotExist() {

        //create ownerId
        Long ownerId = 1L;

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.update(ownerId, itemDto, itemId),
                String.format("Пользователя с id %d не существует", ownerId));

    }

    /**
     * test update method
     * when user exists
     * when item does not exist throw ObjectNotFoundException
     */
    @Test
    public void shouldFailUpdateIfItemDoesNotExist() {

        //create Owner
        Long ownerId = 1L;
        UserDto owner = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(owner);

        //create itemId
        Long itemId = 1L;

        //create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.update(ownerId, itemDto, itemId),
                String.format("Вещи id %d не существует", itemId));

    }

    /**
     * test update method
     * when user exists
     * when item exists
     * when user is not owner throw AccessIsNotAllowedException
     */
    @Test
    public void shouldFailUpdateIfUserIsNotOwner() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.create(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        //create notOwner
        Long notOwnerId = 2L;
        UserDto notOwnerDto = UserDto.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(notOwnerDto);

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        ItemDto savedItemDto = itemService.create(ownerId, itemDto);
        Item item = ItemMapper.toItem(savedItemDto, owner, null);

        //invoke tested method to check throws
        assertThrows(AccessIsNotAllowedException.class,
                () -> itemService.update(notOwnerId, itemDto, itemId),
                String.format("Операция доступна только владельцу вещи %s :", item));

    }

    /**
     * test deleteById method
     * when item exists
     * should delete item by id
     */
    @Test
    public void shouldDeleteItemById() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create itemId
        Long itemId = 1L;

        //tested invoke method
        itemService.deleteById(itemId);

        //invoke getById method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.getById(ownerId, itemId),
                String.format("Вещи id %d не существует", itemId));

    }

    /**
     * test deleteById method
     * when item does not exist
     * should not throw exception
     */
    @Test
    public void shouldDoesNotThrowExceptionIfItemIsNotFoundToDelete() {

        //create itemId
        Long itemId = 1L;

        //tested invoke method
        assertDoesNotThrow(() -> itemService.deleteById(itemId));
    }

    /**
     * test getListByUser
     * should get list of items for owner invoking findAllByOwnerId in item's repository by userId
     * should get last booking info for every item in list by invoking booking's repository
     * should get next booking info for every item in list by invoking booking's repository
     * should get lists of comments invoke findAllByItemIn in comment's repository by item's list for every item
     * should return list of items to user with all necessary information
     */
    @Test
    public void shouldGetListOfItemsByUser() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create requester
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(requesterDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        itemRequestService.create(requesterId, itemRequestDto);

        //create Items
        Long item1Id = 1L;
        ItemDto item1Dto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        itemService.create(ownerId, item1Dto);

        Long item2Id = 2L;
        ItemDto itemDto2 = ItemDto.builder()
                .name("pram")
                .description("old")
                .available(true)
                .build();
        itemService.create(ownerId, itemDto2);

        //create nextBooking for Item1
        Long nextBookingId = 1L;
        BookingDto nextBookingDto = BookingDto.builder()
                .itemId(item1Id)
                .start(LocalDateTime.of(2030, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .build();
        BookingOutDto nextBookingOutDto = bookingService.create(requesterId, nextBookingDto);
        bookingService.updateStatus(nextBookingId, ownerId, true);
        Booking nextBooking = BookingMapper.toBooking(nextBookingOutDto);
        BookingItemDto nextBookingItemDto = BookingMapper.toBookingItemDto(nextBooking);


        //invoke tested method
        List<ItemOutDto> result = itemService.getListByUser(ownerId);

        //check result
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), item1Id);
        assertEquals(result.get(0).getName(), "bike");
        assertEquals(result.get(0).getDescription(), "new");
        assertEquals(result.get(0).getAvailable(), true);
        assertNull(result.get(0).getLastBooking());
        assertEquals(result.get(0).getNextBooking(), nextBookingItemDto);
        assertEquals(result.get(0).getNextBooking().getId(), nextBookingId);
        assertEquals(result.get(0).getComments(), Collections.emptyList());
        assertEquals(result.get(1).getId(), item2Id);
        assertEquals(result.get(1).getName(), "pram");
        assertEquals(result.get(1).getDescription(), "old");
        assertEquals(result.get(1).getAvailable(), true);
        assertNull(result.get(1).getLastBooking());
        assertNull(result.get(1).getNextBooking());
        assertEquals(result.get(1).getComments().size(), 0);
    }

    /**
     * test getListByUser without items
     * should get list of items for owner invoking findAllByOwnerId in item's repository by userId
     * should get lists of comments invoke findAllByItemIn in comment's repository by item's list for every item
     * should not invoke bookingRepository for last and next bookings
     * should return empty list of items
     */
    @Test
    public void shouldGetEmptyListOfItems() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //invoke tested method
        List<ItemOutDto> result = itemService.getListByUser(ownerId);

        //check result
        assertEquals(result, Collections.emptyList());
        assertEquals(result.size(), 0);
    }

    /**
     * test searchItemsBySubstring
     * should get list of items invoking searchItemsBySubstring in item's repository by substring
     * should get last booking info for every item in list by invoking booking's repository
     * should get next booking info for every item in list by invoking booking's repository
     * should get lists of comments invoke findAllByItemIn in comment's repository by item's list for every item
     * should return list of items with all necessary information
     */
    @Test
    public void shouldSearchItemsBySubstring() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.create(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        //create requester
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        UserDto savedRequesterDto = userService.create(requesterDto);
        User requester = UserMapper.toUser(savedRequesterDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        ItemRequestOutDto itemRequestOutDto = itemRequestService.create(requesterId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestOutDto, requester);

        //create Items
        Long item1Id = 1L;
        ItemDto item1Dto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        ItemDto savedItemDto1 = itemService.create(ownerId, item1Dto);
        Item item1 = ItemMapper.toItem(savedItemDto1, owner, itemRequest);
        ItemDto itemDto2 = ItemDto.builder()
                .name("pram")
                .description("old")
                .available(true)
                .build();
        itemService.create(ownerId, itemDto2);

        //create nextBooking for Item1
        Long nextBookingId = 1L;
        BookingDto nextBookingDto = BookingDto.builder()
                .itemId(item1Id)
                .start(LocalDateTime.of(2030, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .build();
        BookingOutDto nextBookingOutDto = bookingService.create(requesterId, nextBookingDto);
        bookingService.updateStatus(nextBookingId, ownerId, true);
        Booking nextBooking = BookingMapper.toBooking(nextBookingOutDto);
        BookingItemDto nextBookingItemDto = BookingMapper.toBookingItemDto(nextBooking);

        //create ItemOutDto objects
        ItemOutDto itemOutDto1 = ItemMapper.toItemOutDto(item1,
                null, nextBookingItemDto, Collections.emptyList());

        //create expected list of items by user
        List<ItemOutDto> expectedItemsListBySearch = List.of(itemOutDto1);

        //invoke tested method
        List<ItemOutDto> result = itemService.searchItemsBySubstring("IkE");

        //check result
        assertEquals(result, expectedItemsListBySearch);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId(), item1Id);
        assertEquals(result.get(0).getName(), "bike");
        assertEquals(result.get(0).getDescription(), "new");
        assertEquals(result.get(0).getAvailable(), true);
        assertNull(result.get(0).getLastBooking());
        assertEquals(result.get(0).getNextBooking(), nextBookingItemDto);
        assertEquals(result.get(0).getNextBooking().getId(), nextBookingId);
        assertEquals(result.get(0).getComments(), Collections.emptyList());

    }


    /**
     * test searchItemsBySubstring
     * should get list of items invoking searchItemsBySubstring in item's repository by substring
     * should get lists of comments invoke findAllByItemIn in comment's repository by item's list for every item
     * should not invoke bookingRepository for last and next bookings
     * should return empty list of items
     */

    @Test
    public void shouldGetEmptyListOfItemsBySearch() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create requester
        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(requesterDto);

        //create ItemRequest
        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .build();
        itemRequestService.create(requesterId, itemRequestDto);

        //create Items
        ItemDto item1Dto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        itemService.create(ownerId, item1Dto);

        ItemDto itemDto2 = ItemDto.builder()
                .name("pram")
                .description("old")
                .available(true)
                .build();
        itemService.create(ownerId, itemDto2);

        //invoke tested method
        List<ItemOutDto> result = itemService.searchItemsBySubstring("text");

        //check result
        assertEquals(result.size(), 0);
    }

    /**
     * test addComment
     * when item does not exist throw ObjectNotFoundException
     */
    @Test
    public void shouldFailAddCommentWhenItemDoesNotExist() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create itemId
        Long itemId = 1L;

        //create CommentDto
        CommentDto commentDto = CommentDto.builder()
                .itemId(itemId)
                .text("bad bike!")
                .build();

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.addComment(commentDto, ownerId, itemId),
                String.format("Вещи с id %d не существует", itemId));
    }

    /**
     * test addComment
     * when item exists get item
     * when user does not exist throws ObjectNotFoundException
     */
    @Test
    public void shouldFailAddCommentWhenUserDoesNotExist() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        userService.create(ownerDto);

        //create Item
        Long itemId = 1L;
        ItemDto item1Dto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        itemService.create(ownerId, item1Dto);

        //create CommentDto
        CommentDto commentDto = CommentDto.builder()
                .itemId(itemId)
                .text("bad bike!")
                .build();

        //create nonExisted id
        Long nonExistedId = -1L;

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.addComment(commentDto, nonExistedId, itemId),
                String.format("Пользователя с id %d не существует", ownerId));

    }

    /**
     * test addComment
     * when item exists get item
     * when user exists get user
     * when user is owner throws AccessIsNotAllowedException
     */
    @Test
    public void shouldFailAddCommentWhenUserIsOwner() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.create(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        itemService.create(ownerId, itemDto);

        //create CommentDto
        CommentDto commentDto = CommentDto.builder()
                .itemId(itemId)
                .authorName(owner.getName())
                .text("bad bike!")
                .build();

        //invoke tested method
        assertThrows(AccessIsNotAllowedException.class,
                () -> itemService.addComment(commentDto, ownerId, itemId),
                "Объект не найден среди доступных для бронирования:"
                        + " владелец не может забронировать свою вещь.");

    }

    /**
     * test addComment
     * when item exists get item
     * when user exists get user
     * when user is not owner
     * when user has not current or past bookings with approved status throws UnavailableItemException
     */
    @Test
    public void shouldFailAddCommentWhenUserHasNotCurrentOrPastBookings() {

        //create Owner
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.create(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        //create Item
        Long itemId = 1L;
        ItemDto item1Dto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        itemService.create(ownerId, item1Dto);

        //create CommentDto
        CommentDto commentDto = CommentDto.builder()
                .itemId(itemId)
                .authorName(owner.getName())
                .text("bad bike!")
                .build();

        //create notOwner;
        Long notOwnerId = 2L;
        UserDto notOwnerDto = UserDto.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        userService.create(notOwnerDto);


        //invoke tested method to check throws
        assertThrows(UnavailableItemException.class,
                () -> itemService.addComment(commentDto, notOwnerId, itemId),
                "Вы не вправе оставлять отзывы, "
                        + " поскольку не пользовались данной вещью.");

    }

}
