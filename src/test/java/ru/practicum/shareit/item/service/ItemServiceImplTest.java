package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentOutDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.exceptions.AccessIsNotAllowedException;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.util.exceptions.UnavailableItemException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ItemServiceImpl tests
 */
@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private CommentRepository commentRepository;


    /**
     * test create method
     * get user when user exists
     * get itemRequest when itemRequest exist by requestId
     * should invoke save in Repository and return result
     */
    @Test
    public void create_whenUserExists_whenRequestIdIsNull_InvokeSave_AndReturnSavedItem() {

        //create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //create item
        Long itemId = 1L;
        Item itemWithoutRequest = ItemMapper.toItem(itemDto, owner, null);
        Item savedItemWithoutRequest = itemWithoutRequest.toBuilder()
                .id(itemId)
                .build();
        ItemDto expectedItem = ItemMapper.toItemDto(savedItemWithoutRequest);

        //mock repository answer
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(itemWithoutRequest)).thenReturn(savedItemWithoutRequest);

        //invoke tested method
        ItemDto result = itemService.create(ownerId, itemDto);

        //check result
        assertEquals(result, expectedItem);

        //verify invokes
        verify(userRepository).findById(ownerId);
        verify(itemRepository).save(itemWithoutRequest);

    }

    /**
     * test create method
     * get user when user exists
     * get itemRequest when itemRequest exist by requestId
     * should invoke save in Repository and return result
     */

    @Test
    public void create_whenUserExists_whenRequestExistsByRequestId_InvokeSave_AndReturnSavedItem() {

        //create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();

        //create Item with id
        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder().id(itemId).build();
        ItemDto expectedItem = ItemMapper.toItemDto(savedItem);

        //mock repository answer
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(item)).thenReturn(savedItem);

        //invoke tested method
        ItemDto result = itemService.create(ownerId, itemDto);


        // verify invokes and their order
        InOrder inOrder = inOrder(userRepository, itemRequestRepository, itemRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRequestRepository).findById(requestId);
        inOrder.verify(itemRepository).save(item);
        inOrder.verifyNoMoreInteractions();

        //check result
        assertEquals(result, expectedItem);

    }

    /**
     * test create method
     * when user does not exist throw ObjectNotFoundException
     * should not invoke any after checking
     */

    @Test
    public void create_whenUserDoesNotExist_thenThrowObjectNotFound_DoesNotInvokeAnyMore() {

        //create ownerId
        Long ownerId = 1L;

        //create itemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //mock repository answer
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        //invoke tested method check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.create(ownerId, itemDto),
                String.format("Пользователя с id %d не существует", ownerId));

        // verify invokes
        verify(userRepository).findById(ownerId);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(itemRepository);

    }

    /**
     * test create method
     * get user when user exists
     * when requestId is not null and request by requestId not found throw ObjectNotFoundException
     * should not invoke any after failed checking
     */

    @Test
    public void create_whenUserExists_RequestIdIsNotNullButNotFound_thenThrowObjectNotFound_DoesNotInvokeAnyMore() {

        //create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create requestId
        Long requestId = 1L;

        // create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .requestId(requestId)
                .available(true)
                .build();

        //mock repository answer
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        //invoke tested method check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.create(ownerId, itemDto),
                String.format("Запроса id %d не существует", requestId));

        // verify invokes
        InOrder inOrder = inOrder(userRepository, itemRequestRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRequestRepository).findById(requestId);

    }

    /**
     * test getById method
     * when item exists
     * when user is not owner set null lastBooking and nextBooking info
     * get comments when they exist
     * construct object ItemOutDto with comments and return result
     */
    @Test
    public void getById_whenUserExists_whenUserIsNotOwner_doesNotGetBookings_addComments_returnConstructedObject() {

        //create owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder().id(itemId).build();

        //create comments for Item
        Comment comment1 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("bad bike!")
                .created(LocalDateTime.now().minusWeeks(1))
                .build();
        Comment comment2 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("I change my mind: good bike!")
                .created(LocalDateTime.now())
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentOutDto> commentsOut = CommentMapper.toCommentOutDtoList(comments);

        //construct expected item to response
        ItemOutDto expectedItemForNotOwner = ItemMapper
                .toItemOutDto(savedItem, null, null, commentsOut);

        //mock repository answer
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(commentRepository.findAllByItemId(itemId)).thenReturn(comments);

        //invoke tested method check throws
        ItemOutDto result = itemService.getById(notOwnerId, itemId);

        // verify invokes
        InOrder inOrder = inOrder(itemRepository, commentRepository);
        inOrder.verify(itemRepository).findById(itemId);
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(eq(itemId),
                        eq(BookingStatus.APPROVED), any(), any());
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(eq(itemId),
                        eq(BookingStatus.APPROVED), any(), any());
        inOrder.verify(commentRepository).findAllByItemId(itemId);
        //check result

        assertEquals(result, expectedItemForNotOwner);
        assertThat(result).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "bike")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("lastBooking", null)
                .hasFieldOrPropertyWithValue("nextBooking", null)
                .hasFieldOrPropertyWithValue("comments", commentsOut);

    }

    /**
     * test getById method
     * when item exists
     * when user is owner get lastBooking and nextBooking when exist
     * get comments when exist
     * construct object ItemOutDto and return result
     */

    @Test
    public void getById_whenUserExists_whenUserIsOwner_getExistingBookingsInfo_addComments_returnConstructedObject() {

        //create owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder().id(itemId).build();

        //create Bookings for Item
        Long lastBookingId = 1L;
        Booking lastBooking = Booking.builder()
                .id(lastBookingId)
                .booker(notOwner)
                .build();
        Long nextBookingId = 2L;
        Booking nextBooking = Booking.builder()
                .id(nextBookingId)
                .booker(notOwner)
                .build();
        BookingItemDto lastBookingDto = BookingMapper.toBookingItemDto(lastBooking);
        BookingItemDto nextBookingDto = BookingMapper.toBookingItemDto(nextBooking);

        //create comments for Item
        Comment comment1 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("bad bike!")
                .created(LocalDateTime.now().minusWeeks(1))
                .build();
        Comment comment2 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("I change my mind: good bike!")
                .created(LocalDateTime.now())
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentOutDto> commentsOut = CommentMapper.toCommentOutDtoList(comments);

        //construct item to response
        ItemOutDto expectedItemForOwner = ItemMapper.toItemOutDto(savedItem, lastBookingDto, nextBookingDto, commentsOut);

        //mock repository answer
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(bookingRepository.findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(eq(itemId),
                eq(BookingStatus.APPROVED), any(), any())).thenReturn(Optional.of(nextBooking));
        when(bookingRepository.findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(eq(itemId),
                eq(BookingStatus.APPROVED), any(), any())).thenReturn(Optional.of(lastBooking));
        when(commentRepository.findAllByItemId(itemId)).thenReturn(comments);

        //invoke tested method check throws
        ItemOutDto result = itemService.getById(ownerId, itemId);

        // verify invokes
        InOrder inOrder = inOrder(itemRepository, commentRepository);
        inOrder.verify(itemRepository).findById(itemId);
        verify(bookingRepository)
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(),
                        any(), any(), any());
        verify(bookingRepository)
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(),
                        any(), any(), any());
        inOrder.verify(commentRepository).findAllByItemId(itemId);

        //check result
        assertEquals(result, expectedItemForOwner);
        assertThat(result).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "bike")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("lastBooking", lastBookingDto)
                .hasFieldOrPropertyWithValue("nextBooking", nextBookingDto)
                .hasFieldOrPropertyWithValue("comments", commentsOut);

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
    public void getById_whenUserExists_whenUserIsOwner_getNullBookingsInfo_whenCommentsExist_returnConstructedObject() {

        //create owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder().id(itemId).build();

        //create comments for Item
        Comment comment1 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("bad bike!")
                .created(LocalDateTime.now().minusWeeks(1))
                .build();
        Comment comment2 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("I change my mind: good bike!")
                .created(LocalDateTime.now())
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentOutDto> commentsOut = CommentMapper.toCommentOutDtoList(comments);

        //construct item to response
        ItemOutDto expectedItemForOwner = ItemMapper.toItemOutDto(savedItem, null, null, commentsOut);

        //mock repository answer

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(bookingRepository.findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(anyLong(),
                any(), any(), any())).thenReturn(Optional.empty());
        when(bookingRepository.findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(anyLong(),
                any(), any(), any())).thenReturn(Optional.empty());
        when(commentRepository.findAllByItemId(itemId)).thenReturn(comments);

        //invoke tested method check throws
        ItemOutDto result = itemService.getById(ownerId, itemId);

        // verify invokes
        InOrder inOrder = inOrder(itemRepository, commentRepository);
        inOrder.verify(itemRepository).findById(itemId);
        verify(bookingRepository)
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(),
                        any(), any(), any());
        verify(bookingRepository)
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(),
                        any(), any(), any());
        inOrder.verify(commentRepository).findAllByItemId(itemId);
        //check result

        assertEquals(result, expectedItemForOwner);
        assertThat(result).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "bike")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("lastBooking", null)
                .hasFieldOrPropertyWithValue("nextBooking", null)
                .hasFieldOrPropertyWithValue("comments", commentsOut);

    }

    /**
     * test getById method
     * when itm does not exist
     * then throws ObjectNotFoundException
     * should not invoke any after fail checking
     */

    @Test
    public void getById_whenItemDoesNotExist_thenThrowObjectNotFound_doesNotInvokeAnyMore() {

        //create ownerId
        Long ownerId = 1L;

        //create Item
        Long itemId = 1L;

        //mock repository answer
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.getById(ownerId, itemId),
                String.format("Запроса с id %d не существует", itemId));

        //verify invokes
        verify(itemRepository).findById(itemId);
    }


    /**
     * test update method
     * when user exists
     * when item exists
     * when user is item's owner
     * when all fields are in presence in itemDto to update and valid
     * should ignore id field
     * should invoke save method in repository
     * should return updated itemDto object
     */

    @Test
    public void update_whenUserExists_ItemExists_AllItemDtoFieldsAreNotNullAndValid_ignoreId_invokeSave_returnUpdatedItem() {

        //create ItemDto to update with valid fields
        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(44L)
                .name("NewName")
                .description("NewDescription")
                .available(false)
                .build();

        //create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //create Item to response
        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        //create expected ItemDto  to response
        Item updatedItem = ItemMapper.toItem(itemDtoToUpdate, owner, itemRequest)
                .toBuilder()
                .id(itemId)
                .build();
        ItemDto updatedItemDto = ItemMapper.toItemDto(updatedItem);

        //mock repository answer
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));

        //invoke tested method
        ItemDto result = itemService.update(ownerId, updatedItemDto, itemId);

        // verify invokes
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository).existsById(ownerId);
        inOrder.verify(itemRepository).findById(ownerId);
        inOrder.verify(itemRepository).save(updatedItem);

        //check result
        assertEquals(result, updatedItemDto);
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
     * should invoke save method in repository
     * should return itemDto object with updated name
     */

    @Test
    public void update_whenUserExists_ItemExists_OnlyValidNameToUpdate_ignoreId_invokeSave_returnUpdatedNameItem() {

        //create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //create Item to response
        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        //create expected ItemDto  to response
        Item updatedItem = ItemMapper.toItem(itemDto, owner, itemRequest)
                .toBuilder()
                .id(itemId)
                .name("NewName")
                .build();
        ItemDto updatedItemDto = ItemMapper.toItemDto(updatedItem);

        //mock repository answer
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));

        //invoke tested method
        ItemDto result = itemService.update(ownerId, updatedItemDto, itemId);

        // verify invokes
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository).existsById(ownerId);
        inOrder.verify(itemRepository).findById(ownerId);
        inOrder.verify(itemRepository).save(updatedItem);

        //check result
        assertEquals(result, updatedItemDto);
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
     * should invoke save method in repository
     * should return itemDto object with updated description
     */

    @Test
    public void update_whenUserExists_ItemExists_OnlyValidDescriptionToUpdate_invokeSave_returnUpdatedDescriptionItem() {

        //create ItemDto to update with only valid description
        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(44L)
                .description("NewDescription")
                .build();

        //create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //create Item to response
        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        //create expected ItemDto  to response
        Item updatedItem = ItemMapper.toItem(itemDto, owner, itemRequest)
                .toBuilder()
                .id(itemId)
                .description(itemDtoToUpdate.getDescription())
                .build();

        ItemDto updatedItemDto = ItemMapper.toItemDto(updatedItem);
        //mock repository answer
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(itemRepository.save(updatedItem)).thenReturn(updatedItem);


        // invoke tested method
        ItemDto result = itemService.update(ownerId, itemDtoToUpdate, itemId);

        // verify invokes
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository).existsById(ownerId);
        inOrder.verify(itemRepository).findById(ownerId);
        inOrder.verify(itemRepository).save(updatedItem);
        inOrder.verifyNoMoreInteractions();

        //check result
        assertEquals(result, updatedItemDto);
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "bike")
                .hasFieldOrPropertyWithValue("description", "NewDescription")
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
     * should invoke save method in repository
     * should return itemDto object with updated available status
     */

    @Test
    public void update_whenUserExists_ItemExists_OnlyValidAvailableToUpdate_ignoreId_invokeSave_returnUpdateAvailableItem() {

        //create ItemDto to update with only valid available
        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(44L)
                .available(false)
                .build();

        //create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //create Item to response
        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        //create expected ItemDto  to response
        Item updatedItem = ItemMapper.toItem(itemDtoToUpdate, owner, null)
                .toBuilder()
                .id(itemId)
                .build();
        ItemDto updatedItemDto = ItemMapper.toItemDto(updatedItem);

        //mock repository answer
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));

        //invoke tested method
        ItemDto result = itemService.update(ownerId, updatedItemDto, itemId);

        // verify invokes
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository).existsById(ownerId);
        inOrder.verify(itemRepository).findById(ownerId);
        //  inOrder.verify(itemRepository).save(updatedItem);

        //check result
        //  assertEquals(result, updatedItemDto);
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
     * should invoke save method in repository
     * should return nonUpdated ItemDto object
     */

    @Test
    public void update_whenUserExists_ItemExists_InvalidFieldsToUpdate_invokeSave_returnNonUpdatedItem() {

        //create ItemDto with invalid fields to update
        ItemDto invalidItemDtoToUpdate = ItemDto.builder()
                .name("")
                .description("")
                .available(null)
                .build();

        //create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //create Item to response
        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();
        ItemDto savedItemDto = ItemMapper.toItemDto(savedItem);

        //create expected ItemDto  to response
        Item updatedItem = ItemMapper.toItem(invalidItemDtoToUpdate, owner, itemRequest)
                .toBuilder()
                .id(itemId)
                .build();
        ItemDto updatedItemDto = ItemMapper.toItemDto(updatedItem);


        //mock repository answers
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));

        //invoke tested method
        ItemDto result = itemService.update(ownerId, updatedItemDto, itemId);

        // verify invokes
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository).existsById(ownerId);
        inOrder.verify(itemRepository).findById(ownerId);
        inOrder.verify(itemRepository).save(savedItem);

        //check result
        assertEquals(result, savedItemDto);
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "bike")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("requestId", requestId);

    }

    /**
     * test update method
     * when user does not exist throw ObjectNotFoundException
     * should not invoke any method after checking
     */
    @Test
    public void update_whenUserDoesNotExist_thenThrowsObjectNotFound_doesNotInvokeAnyMore() {

        //create ownerId
        Long ownerId = 1L;

        //create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //create itemId
        Long itemId = 1L;

        //mock repository answers
        when(userRepository.existsById(ownerId)).thenReturn(false);

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.update(ownerId, itemDto, itemId),
                String.format("Пользователя с id %d не существует", ownerId));

        // verify invokes
        verify(userRepository).existsById(ownerId);
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    /**
     * test update method
     * when user exists
     * when item does not exist throw ObjectNotFoundException
     * should not invoke any method after checking itemId
     */
    @Test
    public void update_whenUserExist_butItemDoesNot_thenThrowsObjectNotFound_doesNotInvokeAnyMore() {

        //create Owner
        Long ownerId = 1L;

        //create itemId
        Long itemId = 1L;

        //create ItemDto
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //mock repository answers
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.update(ownerId, itemDto, itemId),
                String.format("Вещи id %d не существует", itemId));

        // verify invokes
        verify(userRepository).existsById(ownerId);
        verify(itemRepository).findById(itemId);
        verifyNoMoreInteractions(userRepository, itemRepository);

    }

    /**
     * test update method
     * when user exists
     * when item exists
     * when user is not owner throw AccessIsNotAllowedException
     * should not invoke any method after checking user is owner
     */
    @Test
    public void update_whenUserExist_itemExists_userIsNotOwner_thenThrowsAccessIsNotAllowedException_doesNotInvokeAnyMore() {

        //create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwnerId
        Long notOwnerId = 2L;

        //create Item
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        Item item = ItemMapper.toItem(itemDto, owner, null);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        //mock repository answers
        when(userRepository.existsById(notOwnerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));

        //invoke tested method to check throws
        assertThrows(AccessIsNotAllowedException.class,
                () -> itemService.update(notOwnerId, itemDto, itemId),
                String.format("Операция доступна только владельцу вещи %s :", item));

        // verify invokes
        verify(userRepository).existsById(notOwnerId);
        verify(itemRepository).findById(itemId);
        verifyNoMoreInteractions(userRepository, itemRepository);

    }

    /**
     * test deleteById method
     * when item exists
     * should invoke method deleteById in repository
     */
    @Test
    public void delete_whenItemExists_invokeRepository() {

        //create itemId
        Long itemId = 1L;

        //mock repository answers;
        when(itemRepository.existsById(itemId)).thenReturn(true);

        //tested invoke method
        itemService.deleteById(itemId);

        // verify invokes
        verify(itemRepository).deleteById(itemId);
    }


    /**
     * test deleteById method
     * when item does not exist
     * should not invoke deleteById in repository
     */
    @Test
    public void delete_whenItemDoesNotExists_doesNotInvokeRepository() {

        //create itemId
        Long itemId = 1L;

        //mock repository answers;
        when(itemRepository.existsById(itemId)).thenReturn(false);

        //tested invoke method
        itemService.deleteById(itemId);

        // verify invokes
        verify(itemRepository, never()).deleteById(itemId);
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
    public void getListByUser_returnListOfItems() {

        // create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create ItemDto objects
        ItemDto itemDto1 = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();
        ItemDto itemDto2 = ItemDto.builder()
                .name("pram")
                .description("old")
                .available(true)
                .build();

        //create Items
        Long item1Id = 1L;
        Item item1 = ItemMapper.toItem(itemDto1, owner, itemRequest)
                .toBuilder().id(item1Id).build();

        Long item2Id = 2L;
        Item item2 = ItemMapper.toItem(itemDto2, owner, itemRequest)
                .toBuilder().id(item2Id).build();

        List<Item> items = List.of(item1, item2);

        //create lastBooking, next Booking to item1
        Long lastBookingId = 1L;
        Booking lastBooking = Booking.builder()
                .id(lastBookingId)
                .booker(notOwner)
                .build();
        Long nextBookingId = 2L;
        Booking nextBooking = Booking.builder()
                .id(nextBookingId)
                .booker(notOwner)
                .build();
        BookingItemDto lastBookingDto = BookingMapper.toBookingItemDto(lastBooking);
        BookingItemDto nextBookingDto = BookingMapper.toBookingItemDto(nextBooking);

        //create comments to item1
        Long comment1Id = 1L;
        Comment comment1 = Comment.builder()
                .id(comment1Id)
                .item(item1)
                .author(notOwner)
                .text("bad bike!")
                .created(LocalDateTime.now().minusWeeks(1))
                .build();
        Long comment2Id = 2L;
        Comment comment2 = Comment.builder()
                .id(comment2Id)
                .item(item1)
                .author(notOwner)
                .text("I change my mind: good bike!")
                .created(LocalDateTime.now())
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentOutDto> commentsOut = CommentMapper.toCommentOutDtoList(comments);

        //create ItemOutDto objects
        ItemOutDto itemOutDto1 = ItemMapper.toItemOutDto(item1,
                lastBookingDto, nextBookingDto, commentsOut);
        ItemOutDto itemOutDto2 = ItemMapper.toItemOutDto(item2,
                lastBookingDto, nextBookingDto, Collections.emptyList());

        //create expected list of items by user
        List<ItemOutDto> expectedItemsListByUser = List.of(itemOutDto1, itemOutDto2);

        //mock repository answers
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(items);
        when(commentRepository.findAllByItemIn(items)).thenReturn(comments);
        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(any(), any(), any(), any()))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any()))
                .thenReturn(Optional.of(nextBooking));

        //invoke tested method
        List<ItemOutDto> result = itemService.getListByUser(ownerId);

        // verify invokes
        verify(itemRepository).findAllByOwnerId(ownerId);
        verify(commentRepository).findAllByItemIn(items);
        verify(bookingRepository, atLeast(1))
                .findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(any(), any(), any(), any());
        verify(bookingRepository, atLeast(1))
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());

        //check result
        assertEquals(result, expectedItemsListByUser);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), item1Id);
        assertEquals(result.get(0).getName(), "bike");
        assertEquals(result.get(0).getDescription(), "new");
        assertEquals(result.get(0).getAvailable(), true);
        assertEquals(result.get(0).getLastBooking(), lastBookingDto);
        assertEquals(result.get(0).getLastBooking().getId(), lastBookingId);
        assertEquals(result.get(0).getNextBooking(), nextBookingDto);
        assertEquals(result.get(0).getNextBooking().getId(), nextBookingId);
        assertEquals(result.get(0).getComments(), commentsOut);
        assertEquals(result.get(0).getComments().get(0).getId(), comment1Id);
        assertEquals(result.get(0).getComments().get(0).getText(), "bad bike!");
        assertEquals(result.get(0).getComments().get(1).getId(), comment2Id);
        assertEquals(result.get(0).getComments().get(1).getText(), "I change my mind: good bike!");
        assertEquals(result.get(1).getId(), item2Id);
        assertEquals(result.get(1).getName(), "pram");
        assertEquals(result.get(1).getDescription(), "old");
        assertEquals(result.get(1).getAvailable(), true);
        assertEquals(result.get(1).getLastBooking(), lastBookingDto);
        assertEquals(result.get(1).getNextBooking(), nextBookingDto);
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
    public void getListByUserWithoutItems_returnEmptyListOfItems() {

        //create notOwnerId;
        Long notOwnerId = 2L;

        //create empty list of items
        List<ItemOutDto> listByUser = Collections.emptyList();

        //mock repository answers
        when(itemRepository.findAllByOwnerId(notOwnerId)).thenReturn(Collections.emptyList());
        when(commentRepository.findAllByItemIn(Collections.emptyList())).thenReturn(Collections.emptyList());

        //invoke tested method
        List<ItemOutDto> result = itemService.getListByUser(notOwnerId);

        // verify invokes
        verify(itemRepository).findAllByOwnerId(notOwnerId);
        verify(commentRepository).findAllByItemIn(Collections.emptyList());
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());

        //check result
        assertEquals(result, listByUser);
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
    public void searchItemsBySubstring_returnListOfItems() {

        // create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create ItemDto objects
        Long item1Id = 1L;
        ItemDto itemDto1 = ItemDto.builder()
                .id(item1Id)
                .name("bike")
                .description("new")
                .available(true)
                .build();
        Long item2Id = 2L;
        ItemDto itemDto2 = ItemDto.builder()
                .id(item2Id)
                .name("pram")
                .description("old")
                .available(true)
                .build();

        //create Items
        Item item1 = ItemMapper.toItem(itemDto1, owner, itemRequest);
        Item item2 = ItemMapper.toItem(itemDto2, owner, itemRequest);
        List<Item> items = List.of(item1, item2);
        //create lastBooking, next Booking to item1
        Long lastBookingId = 1L;
        Booking lastBooking = Booking.builder()
                .id(lastBookingId)
                .booker(notOwner)
                .build();
        Long nextBookingId = 2L;
        Booking nextBooking = Booking.builder()
                .id(nextBookingId)
                .booker(notOwner)
                .build();
        BookingItemDto lastBookingDto = BookingMapper.toBookingItemDto(lastBooking);
        BookingItemDto nextBookingDto = BookingMapper.toBookingItemDto(nextBooking);

        //create comments to item1
        Long comment1Id = 1L;
        Comment comment1 = Comment.builder()
                .id(comment1Id)
                .item(item1)
                .author(notOwner)
                .text("bad bike!")
                .created(LocalDateTime.now().minusWeeks(1))
                .build();
        Long comment2Id = 2L;
        Comment comment2 = Comment.builder()
                .id(comment2Id)
                .item(item1)
                .author(notOwner)
                .text("I change my mind: good bike!")
                .created(LocalDateTime.now())
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentOutDto> commentsOut = CommentMapper.toCommentOutDtoList(comments);

        //create ItemOutDto objects
        ItemOutDto itemOutDto1 = ItemMapper.toItemOutDto(item1,
                lastBookingDto, nextBookingDto, commentsOut);
        ItemOutDto itemOutDto2 = ItemMapper.toItemOutDto(item2,
                lastBookingDto, nextBookingDto, Collections.emptyList());

        //create expected list of items by user
        List<ItemOutDto> expectedItemsListBySearch = List.of(itemOutDto1, itemOutDto2);

        //mock repository answers
        when(itemRepository.searchItemsBySubstring("text")).thenReturn(items);
        when(commentRepository.findAllByItemIn(items)).thenReturn(comments);
        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(any(), any(), any(), any()))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any()))
                .thenReturn(Optional.of(nextBooking));

        //invoke tested method
        List<ItemOutDto> result = itemService.searchItemsBySubstring("text");

        // verify invokes
        verify(itemRepository).searchItemsBySubstring("text");
        verify(commentRepository).findAllByItemIn(items);
        verify(bookingRepository, atLeast(1))
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());
        verify(bookingRepository, atLeast(1))
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());

        //check result
        assertEquals(result, expectedItemsListBySearch);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), item1Id);
        assertEquals(result.get(0).getName(), "bike");
        assertEquals(result.get(0).getDescription(), "new");
        assertEquals(result.get(0).getAvailable(), true);
        assertEquals(result.get(0).getLastBooking(), lastBookingDto);
        assertEquals(result.get(0).getLastBooking().getId(), lastBookingId);
        assertEquals(result.get(0).getNextBooking(), nextBookingDto);
        assertEquals(result.get(0).getNextBooking().getId(), nextBookingId);
        assertEquals(result.get(0).getComments(), commentsOut);
        assertEquals(result.get(0).getComments().get(0).getId(), comment1Id);
        assertEquals(result.get(0).getComments().get(0).getText(), "bad bike!");
        assertEquals(result.get(0).getComments().get(1).getId(), comment2Id);
        assertEquals(result.get(0).getComments().get(1).getText(), "I change my mind: good bike!");
        assertEquals(result.get(1).getId(), item2Id);
        assertEquals(result.get(1).getName(), "pram");
        assertEquals(result.get(1).getDescription(), "old");
        assertEquals(result.get(1).getAvailable(), true);
        assertEquals(result.get(1).getLastBooking(), lastBookingDto);
        assertEquals(result.get(1).getNextBooking(), nextBookingDto);
        assertEquals(result.get(1).getComments().size(), 0);

    }


    /**
     * test searchItemsBySubstring
     * should get list of items invoking searchItemsBySubstring in item's repository by substring
     * should get lists of comments invoke findAllByItemIn in comment's repository by item's list for every item
     * should not invoke bookingRepository for last and next bookings
     * should return empty list of items
     */

    @Test
    public void searchItemsBySubstringWithoutEmptyResult_returnEmptyListOfItems() {


        //create empty list of items
        List<ItemOutDto> expectedListBySearch = Collections.emptyList();

        //mock repository answers
        when(itemRepository.searchItemsBySubstring("text")).thenReturn(Collections.emptyList());
        when(commentRepository.findAllByItemIn(Collections.emptyList())).thenReturn(Collections.emptyList());

        //invoke tested method
        List<ItemOutDto> result = itemService.searchItemsBySubstring("text");

        // verify invokes
        verify(itemRepository).searchItemsBySubstring("text");
        verify(commentRepository).findAllByItemIn(Collections.emptyList());
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());

        //check result
        assertEquals(result, expectedListBySearch);
        assertEquals(result.size(), 0);
    }

    /**
     * test addComment
     * when item exists get item
     * when user exists get user
     * check user is not owner
     * check user has current or past bookings of the item
     * should invoke save in comment repository
     * should return saved comment
     */
    @Test
    public void addComment_whenItemExists_AndUserExists_AndUserIsNotOwner_AndUserHasPastOrCurrentApprovedBookings() {

        // create Owner
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create notOwner
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create ItemRequest
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent bike")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        //create ItemDto objects
        ItemDto itemDto1 = ItemDto.builder()
                .name("bike")
                .description("new")
                .available(true)
                .build();

        //create Item
        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto1, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        //create lastBooking, next Booking to item
        Long lastBookingId = 1L;
        Booking lastBooking = Booking.builder()
                .id(lastBookingId)
                .booker(notOwner)
                .build();
        Long nextBookingId = 2L;
        Booking nextBooking = Booking.builder()
                .id(nextBookingId)
                .booker(notOwner)
                .build();

        //create comments to item
        Long commentId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .itemId(itemId)
                .authorName(notOwner.getName())
                .text("bad bike!")
                .build();
        Comment comment = CommentMapper.toComment(commentDto, notOwner, savedItem);
        Comment savedComment = comment.toBuilder()
                .id(commentId)
                .build();
        CommentOutDto savedCommentDto = CommentMapper.toCommentOutDto(savedComment);

        //mock repository answers
        when(userRepository.findById(notOwnerId)).thenReturn(Optional.of(notOwner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(bookingRepository.findAllByItem_IdAndBooker_IdAndStatusAndStartIsBefore(anyLong(), any(), any(), any()))
                .thenReturn(List.of(lastBooking, nextBooking));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        //invoke tested method
        CommentOutDto result = itemService.addComment(commentDto, notOwnerId, itemId);

        // verify invokes
        InOrder inOrder = inOrder(userRepository, itemRepository, bookingRepository, commentRepository);

        inOrder.verify(itemRepository).findById(itemId);
        inOrder.verify(userRepository).findById(notOwnerId);
        inOrder.verify(bookingRepository).findAllByItem_IdAndBooker_IdAndStatusAndStartIsBefore(anyLong(),
                any(), any(), any());
        inOrder.verify(commentRepository).save(any(Comment.class));

        //check result
        assertEquals(result, savedCommentDto);
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", commentId)
                .hasFieldOrPropertyWithValue("text", "bad bike!")
                .hasFieldOrPropertyWithValue("authorName", notOwner.getName())
                .hasFieldOrPropertyWithValue("itemId", itemId)
                .hasFieldOrProperty("created");

    }

    /**
     * test addComment
     * when item does not exist throw ObjectNotFoundException
     * should not invoke any repository after checking
     */
    @Test
    public void addComment_whenItemDoesNotExists_thenThrowsObjectNotFound_AndDoesNotInvokeAnyMore() {

        //create itemId
        Long itemId = 1L;

        //create notOwner;
        Long notOwnerId = 1L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create CommentDto
        CommentDto commentDto = CommentDto.builder()
                .itemId(itemId)
                .authorName(notOwner.getName())
                .text("bad bike!")
                .build();

        //mock repository answer
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.addComment(commentDto, notOwnerId, itemId),
                String.format("Вещи с id %d не существует", itemId));

        // verify invokes
        verify(itemRepository).findById(itemId);
        verifyNoMoreInteractions(userRepository, bookingRepository, commentRepository);


    }

    /**
     * test addComment
     * when item exists get item
     * when user does not exist throws ObjectNotFoundException
     * should not invoke any repository after checking
     */
    @Test
    public void addComment_whenItemExists_ButUserNot_thenThrowsObjectNotFound_AndDoesNotInvokeAnyMore() {

        //create Item
        Long itemId = 1L;
        Item savedItem = Item.builder()
                .id(itemId)
                .build();

        //create notOwner;
        Long notOwnerId = 1L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create CommentDto
        CommentDto commentDto = CommentDto.builder()
                .itemId(itemId)
                .authorName(notOwner.getName())
                .text("bad bike!")
                .build();

        //mock repository answers
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(userRepository.findById(notOwnerId)).thenReturn(Optional.empty());

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemService.addComment(commentDto, notOwnerId, itemId),
                String.format("Пользователя с id %d не существует", notOwnerId));

        // verify invokes
        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(notOwnerId);
        verifyNoMoreInteractions(bookingRepository, commentRepository);

    }

    /**
     * test addComment
     * when item exists get item
     * when user exists get user
     * when user is owner throws AccessIsNotAllowedException
     * should not invoke any repository after checking
     */
    @Test
    public void addComment_whenItemExists_andUserIsOwner_thenThrowsAccessNotAllowed_AndDoesNotInvokeAnyMore() {

        //create Owner;
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create Item
        Long itemId = 1L;
        Item savedItem = Item.builder()
                .id(itemId)
                .owner(owner)
                .build();

        //create CommentDto
        CommentDto commentDto = CommentDto.builder()
                .itemId(itemId)
                .authorName(owner.getName())
                .text("bad bike!")
                .build();

        //mock repository answers
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        //invoke tested method
        assertThrows(AccessIsNotAllowedException.class,
                () -> itemService.addComment(commentDto, ownerId, itemId),
                "Объект не найден среди доступных для бронирования:"
                        + " владелец не может забронировать свою вещь.");

        // verify invokes
        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(ownerId);
        verifyNoMoreInteractions(bookingRepository, commentRepository);

    }

    /**
     * test addComment
     * when item exists get item
     * when user exists get user
     * when user is not owner
     * when user has not current or past bookings with approved status throws UnavailableItemException
     * should not invoke any repository after checking
     */
    @Test
    public void addComment_whenItemExists_UserIsNotOwner_butHasNotBookings_thenThrowsUnavailableItem_DoesNotInvokeAnyMore() {

        //create Owner;
        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create Item
        Long itemId = 1L;
        Item savedItem = Item.builder()
                .id(itemId)
                .owner(owner)
                .build();

        //create notOwner;
        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create CommentDto
        CommentDto commentDto = CommentDto.builder()
                .itemId(itemId)
                .authorName(notOwner.getName())
                .text("bad bike!")
                .build();

        //mock repository answers
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(userRepository.findById(notOwnerId)).thenReturn(Optional.of(notOwner));
        when(bookingRepository.findAllByItem_IdAndBooker_IdAndStatusAndStartIsBefore(anyLong(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        //invoke tested method to check throws
        assertThrows(UnavailableItemException.class,
                () -> itemService.addComment(commentDto, notOwnerId, itemId),
                "Вы не вправе оставлять отзывы, "
                        + " поскольку не пользовались данной вещью.");

        // verify invokes
        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(notOwnerId);
        verify(bookingRepository).findAllByItem_IdAndBooker_IdAndStatusAndStartIsBefore(anyLong(),
                any(), any(), any());
        verifyNoMoreInteractions(commentRepository);

    }

}
