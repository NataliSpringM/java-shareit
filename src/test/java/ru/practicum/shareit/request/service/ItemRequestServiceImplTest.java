package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * ItemRequestServiceImpl tests
 */
@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {

    @InjectMocks
    ItemRequestServiceImpl itemRequestService;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Captor
    ArgumentCaptor<ItemRequest> requestCaptor;


    /**
     * create method test
     * when user found
     * should invoke save method in request repository
     * should map result into ItemRequestOutDto and return
     */

    @Test
    public void create_whenUserFound_thenInvokeSave_constructAndReturnResult() {

        //create Requester
        Long requesterId = 1L;
        User requester = User.builder()
                .id(requesterId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create input ItemRequestDto
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();

        //create ItemRequest
        ItemRequest itemRequest = ItemRequestMapper
                .toItemRequest(itemRequestDto, requester);

        //create savedItemRequest
        Long itemRequestId = 1L;
        ItemRequest savedItemRequest = itemRequest.toBuilder()
                .id(itemRequestId)
                .build();

        //create expected out ItemRequestOutDto
        ItemRequestOutDto expectedItemRequest = ItemRequestMapper
                .toItemRequestOutDto(savedItemRequest, null);

        //mock repository answer
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any())).thenReturn(savedItemRequest);

        //invoke tested method
        ItemRequestOutDto result = itemRequestService.create(requesterId, itemRequestDto);

        //verify invoke
        InOrder inOrder = inOrder(userRepository, itemRequestRepository);
        inOrder.verify(userRepository).findById(requesterId);
        inOrder.verify(itemRequestRepository).save(requestCaptor.capture());

        //check result
        assertEquals(result, expectedItemRequest);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemRequestId)
                .hasFieldOrPropertyWithValue("description", "I would like to book bike")
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("items", Collections.emptyList());

        ItemRequest capturedItemRequest = requestCaptor.getValue();
        assertEquals(capturedItemRequest.getDescription(), itemRequest.getDescription());
        assertEquals(capturedItemRequest.getRequester(), requester);
        assertEquals(capturedItemRequest.getCreated().getClass(), LocalDateTime.class);

    }

    /**
     * create method test
     * when user not found
     * then throws ObjectNotFoundException
     * should not invoke save method in request repository
     */

    @Test
    public void create_whenUserNotFound_thenThrowsObjectNotFound_AndDoesNotInvokeSave() {

        //create requesterId;
        Long requesterId = 1L;

        //create input ItemRequestDto
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();

        //mock repository answer
        when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.create(requesterId, itemRequestDto),
                String.format("Пользователя с id %d не существует", requesterId));

        //verify invoke
        verify(userRepository).findById(requesterId);
        verifyNoInteractions(itemRequestRepository);

    }

    /**
     * test getRequestById method
     * when user exists
     * when itemRequest found
     * should invoke getItemByRequestId method in item repository
     * should construct ItemRequestOutDto with items' list and return it
     */
    @Test
    public void getRequestById_whenUserFound_ItemRequestFound_thenReturnItemRequestWithItemsIfTheyPresent() {

        //create Requester
        Long requesterId = 1L;
        User requester = User.builder()
                .id(requesterId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create Owner
        User owner = User.builder()
                .id(requesterId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        // create userId
        Long userId = 3L;

        //create input ItemRequestDto
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        //create ItemRequest
        ItemRequest itemRequest = ItemRequestMapper
                .toItemRequest(itemRequestDto, requester);

        //create savedItemRequest
        Long requestId = 1L;
        ItemRequest savedItemRequest = itemRequest.toBuilder()
                .id(requestId)
                .build();

        //create Items
        Long item1Id = 1L;
        Item item1 = Item.builder()
                .id(item1Id)
                .owner(owner)
                .name("bike")
                .description("new")
                .available(true)
                .request(savedItemRequest)
                .build();
        ItemDto item1dto = ItemMapper.toItemDto(item1);
        Long item2Id = 2L;
        Item item2 = Item.builder()
                .id(item2Id)
                .owner(owner)
                .name("bike")
                .description("old")
                .available(true)
                .request(savedItemRequest)
                .build();
        ItemDto item2dto = ItemMapper.toItemDto(item2);

        //create list of items for request
        List<Item> items = List.of(item1, item2);

        //create list of itemsDto for response
        List<ItemDto> itemsDtoList = List.of(item1dto, item2dto);

        //create expected out ItemRequestOutDto
        ItemRequestOutDto expectedItemRequest = ItemRequestMapper
                .toItemRequestOutDto(savedItemRequest, itemsDtoList);

        //mock repository answer
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(savedItemRequest));
        when(itemRepository.findAllByRequestId(requestId)).thenReturn(items);

        //invoke tested method
        ItemRequestOutDto result = itemRequestService.getRequestById(userId, requestId);

        //verify invoke
        InOrder inOrder = inOrder(userRepository, itemRequestRepository, itemRepository);
        inOrder.verify(userRepository).existsById(userId);
        inOrder.verify(itemRequestRepository).findById(requestId);
        inOrder.verify(itemRepository).findAllByRequestId(requestId);

        //check result
        assertEquals(result, expectedItemRequest);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("description", "I would like to book bike")
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("items", itemsDtoList);

        assertEquals(result.getItems().get(0), item1dto);
        assertEquals(result.getItems().get(1), item2dto);

    }

    /**
     * test getRequestById method
     * when user does not exist
     * then throws ObjectNotFoundException
     * should not invoke getItemByRequestId method in item repository
     */

    @Test
    public void getRequestById_whenUserDoesNotExist_thenThrowsObjectNotFound_doesNotInvokeAnyMore() {

        //create userId
        Long userId = 1L;

        //create requestId
        Long requestId = 1L;

        //mock repository answer
        when(userRepository.existsById(userId)).thenReturn(false);

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getRequestById(userId, requestId),
                String.format("Пользователя с id %d не существует", userId));

        //verify invoke
        verify(userRepository).existsById(userId);
        verifyNoInteractions(itemRequestRepository);
    }

    /**
     * test getRequestById method
     * when user exists
     * when itemRequest not found
     * then throws ObjectNotFoundException
     */

    @Test
    public void getRequestById_whenUserFound_whenItemRequestNotFound_thenThrowsObjectNotFound() {

        //create userId
        Long userId = 1L;

        //create requestId
        Long requestId = 1L;

        //mock repository answer
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getRequestById(userId, requestId),
                String.format("Запроса с id %d не существует", requestId));

        //verify invokes and their order
        InOrder inOrder = inOrder(userRepository, itemRequestRepository);
        inOrder.verify(userRepository).existsById(userId);
        inOrder.verify(itemRequestRepository).findById(requestId);
    }

    /**
     * test getOwnRequests method
     * when user exists
     * should invoke findAllByRequesterIdOrderByCreatedDesc method in requests' repository
     * should invoke findAllByRequestIn in items' repository
     * should construct ItemRequestOut objects with items and return resulting list
     */

    @Test
    public void getOwnRequests_whenUserExists_invokeRequestRepository_invokeItemRepository_constructAndReturnList() {

        //create Requester
        Long requesterId = 1L;
        User requester = User.builder()
                .id(requesterId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        //create Owner
        Long ownerId = 2L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create input ItemRequestDto objects
        ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        ItemRequestDto itemRequestDto2 = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        ItemRequestDto itemRequestDto3 = ItemRequestDto.builder()
                .description("I need jet")
                .build();

        //create ItemRequests
        ItemRequest itemRequest1 = ItemRequestMapper
                .toItemRequest(itemRequestDto1, requester);
        ItemRequest itemRequest2 = ItemRequestMapper
                .toItemRequest(itemRequestDto2, requester);
        ItemRequest itemRequest3 = ItemRequestMapper
                .toItemRequest(itemRequestDto3, requester);

        //create savedItemRequests
        Long itemRequest1Id = 1L;
        ItemRequest savedItemRequest1 = itemRequest1.toBuilder()
                .id(itemRequest1Id)
                .build();
        Long itemRequest2Id = 2L;
        ItemRequest savedItemRequest2 = itemRequest2.toBuilder()
                .id(itemRequest2Id)
                .build();
        Long itemRequest3Id = 3L;
        ItemRequest savedItemRequest3 = itemRequest3.toBuilder()
                .id(itemRequest3Id)
                .build();

        //create list of ItemRequests
       List<ItemRequest> itemRequests = List.of(savedItemRequest1, savedItemRequest2, savedItemRequest3);

        //create Items
        Long item1Id = 1L;
        Item item1 = Item.builder()
                .id(item1Id)
                .owner(owner)
                .name("bike")
                .description("new")
                .available(true)
                .request(savedItemRequest1)
                .build();
        ItemDto itemDto1 = ItemMapper.toItemDto(item1);

        Long item2Id = 2L;
        Item item2 = Item.builder()
                .id(item2Id)
                .owner(owner)
                .name("bike")
                .description("old")
                .available(true)
                .request(savedItemRequest2)
                .build();
        ItemDto itemDto2 = ItemMapper.toItemDto(item2);

        Long item3Id = 3L;
        Item item3 = Item.builder()
                .id(item3Id)
                .owner(owner)
                .name("jet")
                .available(true)
                .request(savedItemRequest3)
                .build();
        ItemDto itemDto3 = ItemMapper.toItemDto(item3);

        //create list of items for requests
        List<ItemDto> request1Items = List.of(itemDto1);
        List<ItemDto> request2Items = List.of(itemDto2);
        List<ItemDto> request3Items = List.of(itemDto3);

        //create list of all items of requester
        List<Item> allItems = List.of(item1, item2, item3);

        //create ItemRequestOutDto objects
        ItemRequestOutDto expectedItemRequest1 = ItemRequestMapper
                .toItemRequestOutDto(savedItemRequest1, request1Items);
        ItemRequestOutDto expectedItemRequest2 = ItemRequestMapper
                .toItemRequestOutDto(savedItemRequest2, request2Items);
        ItemRequestOutDto expectedItemRequest3 = ItemRequestMapper
                .toItemRequestOutDto(savedItemRequest3, request3Items);

        //create expected List of itemRequests with items
        List<ItemRequestOutDto> expectedItemRequests = List.of(expectedItemRequest1,
                expectedItemRequest2, expectedItemRequest3);

        //mock repository answer
        when(userRepository.existsById(requesterId)).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(requesterId))
                .thenReturn(itemRequests);
        when(itemRepository.findAllByRequestIn(itemRequests)).thenReturn(allItems);

        //invoke tested method
        List<ItemRequestOutDto> result = itemRequestService.getOwnRequests(requesterId);

        //verify invoke
        InOrder inOrder = inOrder(userRepository, itemRequestRepository, itemRepository);
        inOrder.verify(userRepository).existsById(requesterId);
        inOrder.verify(itemRequestRepository).findAllByRequesterIdOrderByCreatedDesc(requesterId);
        inOrder.verify(itemRepository).findAllByRequestIn(itemRequests);

        //check result
        assertEquals(result, expectedItemRequests);

        assertThat(result).asList()
                .hasSize(3);

        assertEquals(result.get(0), expectedItemRequest1);
        assertEquals(result.get(0).getItems().size(), 1);
        assertEquals(result.get(0).getDescription(), "I would like to book bike");
        assertEquals(result.get(0).getItems().get(0).getName(), "bike");
        assertEquals(result.get(0).getItems().get(0).getDescription(), "new");
        assertEquals(result.get(1), expectedItemRequest2);
        assertEquals(result.get(1).getItems().size(), 1);
        assertEquals(result.get(1).getDescription(), "I would like to book bike");
        assertEquals(result.get(1).getItems().get(0).getName(), "bike");
        assertEquals(result.get(1).getItems().get(0).getDescription(), "old");
        assertEquals(result.get(2), expectedItemRequest3);
        assertEquals(result.get(2).getItems().size(), 1);
        assertEquals(result.get(2).getDescription(), "I need jet");
        assertEquals(result.get(2).getItems().get(0).getName(), "jet");

    }

    /**
     * test getOwnRequests method
     * when user does not exist
     * should not invoke any more after checking
     */

    @Test
    public void getOwnRequests_whenUserDoesNotExist_thenThrowObjectNotFound_doesNotInvokeAnyMore() {

        //create userId
        Long userId = 1L;

        //mock repository answer
        when(userRepository.existsById(userId)).thenReturn(false);

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getOwnRequests(userId),
                String.format("Пользователя с id %d не существует", userId));

        //verify invoke
        verify(userRepository).existsById(userId);
        verifyNoMoreInteractions(itemRequestRepository, itemRepository);

    }

    /**
     * test getOtherUsersRequests method
     * when user exists
     * should invoke findAllByRequesterIdIsNotOrderByCreatedDesc method in requests' repository
     * should invoke findAllByRequestIn in items' repository
     * should construct ItemRequestOut objects with items and return resulting list
     */

    @Test
    public void getOtherUserRequests_whenUserExists_invokeRequestRepository_invokeItemRepository_constructAndReturnList() {

        //create Owner
        Long ownerId = 2L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();

        //create input ItemRequestDto objects
        ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        ItemRequestDto itemRequestDto2 = ItemRequestDto.builder()
                .description("I need jet")
                .build();

        //create ItemRequests
        ItemRequest itemRequest1 = ItemRequestMapper
                .toItemRequest(itemRequestDto1, owner);
        ItemRequest itemRequest2 = ItemRequestMapper
                .toItemRequest(itemRequestDto2, owner);

        //create savedItemRequests
        Long itemRequest1Id = 1L;
        ItemRequest savedItemRequest1 = itemRequest1.toBuilder()
                .id(itemRequest1Id)
                .build();
        Long itemRequest2Id = 2L;
        ItemRequest savedItemRequest2 = itemRequest2.toBuilder()
                .id(itemRequest2Id)
                .build();

        //create list of ItemRequests
        List<ItemRequest> itemRequests = List.of(savedItemRequest1, savedItemRequest2);

        //create Items
        Long item1Id = 1L;
        Item item1 = Item.builder()
                .id(item1Id)
                .owner(owner)
                .name("bike")
                .description("new")
                .available(true)
                .request(savedItemRequest1)
                .build();
        ItemDto itemDto1 = ItemMapper.toItemDto(item1);

        Long item2Id = 2L;
        Item item2 = Item.builder()
                .id(item2Id)
                .owner(owner)
                .name("bike")
                .description("old")
                .available(true)
                .request(savedItemRequest1)
                .build();
        ItemDto itemDto2 = ItemMapper.toItemDto(item2);

        Long item3Id = 3L;
        Item item3 = Item.builder()
                .id(item3Id)
                .owner(owner)
                .name("jet")
                .available(true)
                .request(savedItemRequest2)
                .build();
        ItemDto itemDto3 = ItemMapper.toItemDto(item3);

        //create list of items for requests
        List<ItemDto> request1Items = List.of(itemDto1, itemDto2);
        List<ItemDto> request2Items = List.of(itemDto3);

        //create list of all items in requests
        List<Item> allItems = List.of(item1, item2, item3);

        //create ItemRequestOutDto objects
        ItemRequestOutDto expectedItemRequest1 = ItemRequestMapper
                .toItemRequestOutDto(savedItemRequest1, request1Items);
        ItemRequestOutDto expectedItemRequest2 = ItemRequestMapper
                .toItemRequestOutDto(savedItemRequest2, request2Items);

        //create expected List of itemRequests with items
        List<ItemRequestOutDto> expectedItemRequests = List.of(expectedItemRequest1, expectedItemRequest2);

        //create page param
        int from = 10;
        int size = 10;
        PageRequest page = PageRequest.of(from / size, size);

        //mock repository answers
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterIdIsNotOrderByCreatedDesc(ownerId, page))
                .thenReturn(itemRequests);
        when(itemRepository.findAllByRequestIn(itemRequests)).thenReturn(allItems);

        //invoke tested method
        List<ItemRequestOutDto> result = itemRequestService.getOtherUsersRequests(ownerId, from, size);

        //verify invoke
        InOrder inOrder = inOrder(userRepository, itemRequestRepository, itemRepository);
        inOrder.verify(userRepository).existsById(ownerId);
        inOrder.verify(itemRequestRepository).findAllByRequesterIdIsNotOrderByCreatedDesc(ownerId, page);
        inOrder.verify(itemRepository).findAllByRequestIn(itemRequests);

        //check result
        assertEquals(result, expectedItemRequests);

        assertThat(result).asList()
                .hasSize(2);

        assertEquals(result.get(0), expectedItemRequest1);
        assertEquals(result.get(0).getItems().size(), 2);
        assertEquals(result.get(0).getDescription(), "I would like to book bike");
        assertEquals(result.get(0).getItems().get(0).getName(), "bike");
        assertEquals(result.get(0).getItems().get(1).getName(), "bike");
        assertEquals(result.get(0).getItems().get(0).getDescription(), "new");
        assertEquals(result.get(1), expectedItemRequest2);
        assertEquals(result.get(1).getItems().size(), 1);
        assertEquals(result.get(1).getDescription(), "I need jet");
        assertEquals(result.get(1).getItems().get(0).getName(), "jet");

    }

    /**
     * test getOtherUserRequests method
     * when user exists
     * should not invoke any more after checking
     */

    @Test
    public void getOtherUsersRequests_whenUserDoesNotExist_thenThrowObjectNotFound_doesNotInvokeAnyMore() {

        //create userId, page parameters
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        //mock repository answer
        when(userRepository.existsById(userId)).thenReturn(false);

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getOtherUsersRequests(userId, from, size),
                String.format("Пользователя с id %d не существует", userId));

        //verify invoke
        verify(userRepository).existsById(userId);
        verifyNoMoreInteractions(itemRequestRepository, itemRepository);

    }


}

