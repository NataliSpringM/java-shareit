package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.exceptions.ObjectNotFoundException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ItemRequestService Integration tests
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    /**
     * test create and getByRequestId method
     */
    @Test
    public void shouldCreateItemRequest_andGetByExistingId() {

        //create Requester
        Long requesterId = 1L;
        User requester = User.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        UserDto requesterDto = UserMapper.toUserDto(requester);

        // save Requester
        userService.create(requesterDto);

        //create input ItemRequestDto
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();

        //create requestId
        Long requestId = 1L;

        //invoke tested method create
        ItemRequestOutDto result = itemRequestService.create(requesterId, itemRequestDto);

        //check result
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("description", "I would like to book bike")
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("items", Collections.emptyList());

        //invoke tested method getByRequestId
        ItemRequestOutDto resultByGet = itemRequestService.getRequestById(requesterId, requestId);

        //check resultByGet
        assertThat(resultByGet)
                .hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("description", "I would like to book bike")
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("items", Collections.emptyList());


    }

    /**
     * test create method
     * when user(requester) does not exist should throw ObjectNotFoundException
     */
    @Test
    public void shouldFailCreate_ThrowExceptionUserNotFound() {

        //create requesterId
        Long requesterId = 1L;

        //create input ItemRequestDto
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.create(requesterId, itemRequestDto),
                String.format("Пользователя с id %d не существует", requesterId));

    }

    /**
     * test getByRequestId method
     * when user exists
     * when request not found should throw ObjectNotFoundException
     */
    @Test
    public void shouldFailGetByRequestId_throwExceptionRequestNotFound() {

        //create Requester
        Long requesterId = 1L;
        User requester = User.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        UserDto requesterDto = UserMapper.toUserDto(requester);

        // save Requester
        userService.create(requesterDto);

        //create requestId
        Long requestId = 1L;

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getRequestById(requesterId, requestId),
                String.format("Запроса с id %d не существует", requestId));

    }

    /**
     * test getOwnRequests method
     * when user exists
     * should return list of itemRequests and items for requester
     */
    @Test
    public void shouldGetOwnRequests() {

        //create and save Requester
        Long requesterId = 1L;
        User requester = User.builder()
                .id(requesterId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        UserDto requesterDto = UserMapper.toUserDto(requester);
        userService.create(requesterDto);

        //create and save Owner
        Long ownerId = 2L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto ownerDto = UserMapper.toUserDto(owner);
        userService.create(ownerDto);

        //create and save ItemRequest objects
        Long itemRequest1Id = 1L;
        ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        ItemRequest savedRequest1 = ItemRequestMapper.toItemRequest(itemRequestDto1, requester)
                .toBuilder()
                .id(itemRequest1Id)
                .build();
        itemRequestService.create(requesterId, itemRequestDto1);
        Long itemRequest2Id = 2L;
        ItemRequestDto itemRequestDto2 = ItemRequestDto.builder()
                .description("I need jet")
                .build();
        ItemRequest savedRequest2 = ItemRequestMapper.toItemRequest(itemRequestDto2, requester)
                .toBuilder()
                .id(itemRequest2Id)
                .build();
        itemRequestService.create(requesterId, itemRequestDto2);
        Long itemRequest3Id = 3L;
        ItemRequestDto itemRequestDto3 = ItemRequestDto.builder()
                .description("I want to fly, I need jet")
                .build();
        ItemRequest savedRequest3 = ItemRequestMapper.toItemRequest(itemRequestDto3, requester)
                .toBuilder()
                .id(itemRequest3Id)
                .build();
        itemRequestService.create(requesterId, itemRequestDto3);

        //create and save Items
        Item item1 = Item.builder()
                .owner(owner)
                .name("bike")
                .description("new")
                .available(true)
                .request(savedRequest1)
                .build();
        ItemDto itemDto1 = ItemMapper.toItemDto(item1);
        itemService.create(ownerId, itemDto1);


        Long item2Id = 2L;
        Item item2 = Item.builder()
                .id(item2Id)
                .owner(owner)
                .name("bike")
                .description("old")
                .available(true)
                .request(savedRequest2)
                .build();
        ItemDto itemDto2 = ItemMapper.toItemDto(item2);
        itemService.create(ownerId, itemDto2);

        Long item3Id = 3L;
        Item item3 = Item.builder()
                .id(item3Id)
                .owner(owner)
                .name("jet")
                .available(true)
                .description("superJet")
                .request(savedRequest3)
                .build();
        ItemDto itemDto3 = ItemMapper.toItemDto(item3);
        itemService.create(ownerId, itemDto3);

        //invoke tested method
        List<ItemRequestOutDto> result = itemRequestService.getOwnRequests(requesterId);

        //check result
        assertThat(result).asList().hasSize(3);

        assertEquals(result.get(0).getItems().size(), 1);
        assertEquals(result.get(0).getDescription(), "I want to fly, I need jet");
        assertEquals(result.get(0).getItems().get(0).getName(), "jet");
        assertEquals(result.get(0).getItems().get(0).getDescription(), "superJet");
        assertEquals(result.get(0).getItems().get(0).getAvailable(), true);
        assertEquals(result.get(1).getItems().size(), 1);
        assertEquals(result.get(1).getDescription(), "I need jet");
        assertEquals(result.get(1).getItems().get(0).getName(), "bike");
        assertEquals(result.get(1).getItems().get(0).getDescription(), "old");
        assertEquals(result.get(1).getItems().get(0).getAvailable(), true);
        assertEquals(result.get(2).getItems().size(), 1);
        assertEquals(result.get(2).getDescription(), "I would like to book bike");
        assertEquals(result.get(2).getItems().get(0).getName(), "bike");
        assertEquals(result.get(2).getItems().get(0).getDescription(), "new");
        assertEquals(result.get(2).getItems().get(0).getAvailable(), true);

    }

    /**
     * test getOwnRequests method
     * when user exists
     * should return list of itemRequests and items
     */
    @Test
    public void shouldFailGetOwnRequestsIfUserNotFound() {

        //create parameters of page
        int from = 0;
        int size = 10;

        //create requesterId
        Long requesterId = 1L;

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getOtherUsersRequests(requesterId, from, size),
                String.format("Пользователя с id %d не существует", requesterId));

    }

    @Test
    public void shouldGetOtherUsersRequests() {

        //create parameters of page
        int from = 0;
        int size = 10;

        //create and save Requester
        Long requesterId = 1L;
        User requester = User.builder()
                .id(requesterId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        UserDto requesterDto = UserMapper.toUserDto(requester);
        userService.create(requesterDto);

        //create and save Owner
        Long ownerId = 2L;
        User owner = User.builder()
                .id(ownerId)
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        UserDto ownerDto = UserMapper.toUserDto(owner);
        userService.create(ownerDto);

        //create and save ItemRequest objects
        Long itemRequest1Id = 1L;
        ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
                .description("I would like to book bike")
                .build();
        ItemRequest savedRequest1 = ItemRequestMapper.toItemRequest(itemRequestDto1, requester)
                .toBuilder()
                .id(itemRequest1Id)
                .build();
        itemRequestService.create(requesterId, itemRequestDto1);
        Long itemRequest2Id = 2L;
        ItemRequestDto itemRequestDto2 = ItemRequestDto.builder()
                .description("I need jet")
                .build();
        ItemRequest savedRequest2 = ItemRequestMapper.toItemRequest(itemRequestDto2, requester)
                .toBuilder()
                .id(itemRequest2Id)
                .build();
        itemRequestService.create(requesterId, itemRequestDto2);
        Long itemRequest3Id = 3L;
        ItemRequestDto itemRequestDto3 = ItemRequestDto.builder()
                .description("I want to fly, I need jet")
                .build();
        ItemRequest savedRequest3 = ItemRequestMapper.toItemRequest(itemRequestDto3, requester)
                .toBuilder()
                .id(itemRequest3Id)
                .build();
        itemRequestService.create(requesterId, itemRequestDto3);

        //create and save Items

        Item item1 = Item.builder()
                .owner(owner)
                .name("bike")
                .description("new")
                .available(true)
                .request(savedRequest1)
                .build();
        ItemDto itemDto1 = ItemMapper.toItemDto(item1);
        itemService.create(ownerId, itemDto1);


        Long item2Id = 2L;
        Item item2 = Item.builder()
                .id(item2Id)
                .owner(owner)
                .name("bike")
                .description("old")
                .available(true)
                .request(savedRequest2)
                .build();
        ItemDto itemDto2 = ItemMapper.toItemDto(item2);
        itemService.create(ownerId, itemDto2);

        Long item3Id = 3L;
        Item item3 = Item.builder()
                .id(item3Id)
                .owner(owner)
                .name("jet")
                .available(true)
                .description("superJet")
                .request(savedRequest3)
                .build();
        ItemDto itemDto3 = ItemMapper.toItemDto(item3);
        itemService.create(ownerId, itemDto3);

        //invoke tested method
        List<ItemRequestOutDto> result = itemRequestService.getOtherUsersRequests(ownerId, from, size);

        assertThat(result).asList().hasSize(3);

        assertEquals(result.get(0).getItems().size(), 1);
        assertEquals(result.get(0).getDescription(), "I want to fly, I need jet");
        assertEquals(result.get(0).getItems().get(0).getName(), "jet");
        assertEquals(result.get(0).getItems().get(0).getDescription(), "superJet");
        assertEquals(result.get(0).getItems().get(0).getAvailable(), true);
        assertEquals(result.get(1).getItems().size(), 1);
        assertEquals(result.get(1).getDescription(), "I need jet");
        assertEquals(result.get(1).getItems().get(0).getName(), "bike");
        assertEquals(result.get(1).getItems().get(0).getDescription(), "old");
        assertEquals(result.get(1).getItems().get(0).getAvailable(), true);
        assertEquals(result.get(2).getItems().size(), 1);
        assertEquals(result.get(2).getDescription(), "I would like to book bike");
        assertEquals(result.get(2).getItems().get(0).getName(), "bike");
        assertEquals(result.get(2).getItems().get(0).getDescription(), "new");
        assertEquals(result.get(2).getItems().get(0).getAvailable(), true);

    }

    /**
     * test getOtherUserRequests method
     * when user(requester) does not exist should throw ObjectNotFoundException
     */
    @Test
    public void shouldFailGetOtherUserRequestsIfUserNotFound() {

        //create parameters of page
        int from = 0;
        int size = 10;

        //create requesterId
        Long requesterId = 1L;

        //invoke tested method to check throws
        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getOtherUsersRequests(requesterId, from, size),
                String.format("Пользователя с id %d не существует", requesterId));

    }


}
