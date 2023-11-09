package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ItemRequest Service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    /**
     * to add item's request (save and assign identity)
     * throw 404.NOT FOUND ObjectNotFoundException if user is not found
     *
     * @param userId         owner's id
     * @param itemRequestDto item's request to save and register
     * @return item's request with assigned id
     */
    @Override
    @Transactional
    public ItemRequestOutDto create(Long userId, ItemRequestDto itemRequestDto) {
        User requester = getUserByIdIfExists(userId);

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, requester);
        ItemRequest itemRequestWithId = itemRequestRepository.save(itemRequest);
        log.info("Сохранена информация о запросе: {}", itemRequestWithId);
        return ItemRequestMapper.toItemRequestOutDto(itemRequestWithId, null);
    }

    /**
     * to get item's request by id
     * throw 404.NOT FOUND ObjectNotFoundException if user doesn't exist
     * throw 404.NOT FOUND ObjectNotFoundException if request doesn't exist
     *
     * @param requestId item's request id
     * @param userId    user's id
     * @return item's request
     */
    @Override
    @Transactional(readOnly = true)
    public ItemRequestOutDto getRequestById(Long userId, Long requestId) {
        checkUserExists(userId);
        ItemRequest itemRequest = getItemRequestByIdIfExists(requestId);

        List<ItemDto> items = getItemsByRequestId(requestId);
        ItemRequestOutDto itemRequestOutDto = ItemRequestMapper.toItemRequestOutDto(itemRequest, items);
        log.info("Найден запрос с id {}: {}", requestId, itemRequestOutDto);
        return itemRequestOutDto;
    }


    /**
     * to get list of the items' requests and answers to them from a specific user
     * throw 404.NOT FOUND ObjectNotFoundException if user is not found
     *
     * @param userId requester id
     * @return list of item's requests with answers to them
     */
    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestOutDto> getOwnRequests(Long userId) {
        checkUserExists(userId);

        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);
        Map<ItemRequest, List<ItemDto>> map = getAllItemsForListRequests(itemRequests);

        List<ItemRequestOutDto> requests = itemRequests.stream()
                .map(item -> ItemRequestMapper
                        .toItemRequestOutDto(item, map.getOrDefault(item, Collections.emptyList())))
                .collect(Collectors.toList());
        logResultList(requests);
        return requests;
    }

    /**
     * to get list of the other users' item's requests to answer
     * list should be started with the newest requests
     * with paging option: size of the page is defined by from/to parameters of request
     *
     * @param userId requester id
     * @param from   first index of the request at the page
     * @param size   last index of the request at the page
     * @return list of the other users' item's requests
     */
    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestOutDto> getOtherUsersRequests(Long userId, Integer from, Integer size) {
        checkUserExists(userId);
        int page = from / size;
        Pageable pageRequest = PageRequest.of(page, size);

        List<ItemRequest> itemRequests = itemRequestRepository
                .findAllByRequesterIdIsNotOrderByCreatedDesc(userId, pageRequest);

        Map<ItemRequest, List<ItemDto>> map = getAllItemsForListRequests(itemRequests);

        List<ItemRequestOutDto> requests = itemRequests.stream()
                .map(item -> ItemRequestMapper
                        .toItemRequestOutDto(item, map.getOrDefault(item, Collections.emptyList())))
                .collect(Collectors.toList());
        logResultList(requests);
        return requests;
    }


    /**
     * get map List<ItemDto> objects in response to ItemRequest
     *
     * @param itemRequests items' requests list
     * @return mapping list of ItemDto objects corresponding with ItemRequest
     */
    private Map<ItemRequest, List<ItemDto>> getAllItemsForListRequests(List<ItemRequest> itemRequests) {

        List<Item> items = itemRepository.findAllByRequestIn(itemRequests);

        return items.stream()
                .collect(Collectors
                        .groupingBy(Item::getRequest,
                                Collectors
                                        .mapping(ItemMapper::toItemDto, Collectors.toList())));

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
     * get ItemRequest if exists throw 404.NOT FOUND ObjectNotFoundException if isn't
     *
     * @param requestId request's id
     * @return ItemRequest object
     */
    private ItemRequest getItemRequestByIdIfExists(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(String.format("Запроса с id %d не существует", requestId)));
    }

    /**
     * get list of items by request's id
     *
     * @param requestId request's id
     * @return list of items by request's id
     */

    private List<ItemDto> getItemsByRequestId(Long requestId) {
        List<Item> itemsByRequestId = itemRepository.findAllByRequestId(requestId);
        return itemsByRequestId.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
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
     * log list of requests in pretty format
     *
     * @param requests list of requests
     */
    private void logResultList(List<ItemRequestOutDto> requests) {
        String result = requests.stream()
                .map(ItemRequestOutDto::toString)
                .collect(Collectors.joining(", "));
        log.info("Список запросов на вещи: {}", result);

    }

}
