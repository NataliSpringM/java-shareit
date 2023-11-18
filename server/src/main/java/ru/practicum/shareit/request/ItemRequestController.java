package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * processing HTTP-requests to "/requests" end-point to add, update or get requests' data.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = REQUESTS_PATH)
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    /**
     * processing POST-request to add item's request (save and assign identity)
     *
     * @param userId         requester id
     * @param itemRequestDto item's request to save and register
     * @return item's request with assigned id
     */
    @PostMapping
    public ItemRequestOutDto create(@RequestHeader(HEADER_USER_ID) Long userId,
                                    @RequestBody ItemRequestDto itemRequestDto) {
        log.info("POST-request: создание запроса от пользователя c id: {}, {}", userId, itemRequestDto);
        return itemRequestService.create(userId, itemRequestDto);
    }

    /**
     * processing GET-request from a specific user to get list of the items' requests and answers to them
     *
     * @param userId requester id
     * @return list of the user's item's requests with answers to them
     */
    @GetMapping
    public List<ItemRequestOutDto> getOwnRequests(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("GET-request: запрос на получение информации о своих запросах и ответах на них "
                + "от пользователя с id {}", userId);
        return itemRequestService.getOwnRequests(userId);
    }

    /**
     * processing GET-request from a specific user to get list of the other users' items requests to answer
     * list should be started with the newest requests
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     *
     * @param userId requester id
     * @param from   first index of the request at the page
     * @param size   size of the page
     * @return list of the other users' item's requests
     */
    @GetMapping(ALL_PATH)
    public List<ItemRequestOutDto> getOtherUsersRequests(@RequestHeader(HEADER_USER_ID) Long userId,
                                                         @RequestParam(
                                                                 name = FROM_PARAMETER_NAME,
                                                                 defaultValue = ZERO_DEFAULT_VALUE) Integer from,
                                                         @RequestParam(
                                                                 name = SIZE_PARAMETER_NAME,
                                                                 defaultValue = TEN_DEFAULT_VALUE) Integer size) {
        log.info("GET-request: запрос на получение информации о всех запросах на вещи "
                + "от пользователя с id {}, начиная с позиции {}, ограничение размера: {}", userId, from, size);
        return itemRequestService.getOtherUsersRequests(userId, from, size);
    }

    /**
     * processing GET-request to get item's request by id
     *
     * @param requestId item's request id
     * @param userId    user's id
     * @return item's request
     */
    @GetMapping(REQUEST_ID_PATH_VARIABLE)
    public ItemRequestOutDto getRequestById(@RequestHeader(HEADER_USER_ID) Long userId,
                                            @PathVariable Long requestId) {
        log.info("GET-request: запрос на получение информации от пользователя {} о запросе с id {}", userId, requestId);
        return itemRequestService.getRequestById(userId, requestId);
    }
}
