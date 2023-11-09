package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Sprint add-item-requests.
 * processing HTTP-requests to "/requests" end-point to add, update or get requests' data.
 */
@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    /**
     * processing POST-request to add item's request (save and assign identity)
     *
     * @param userId         requester id
     * @param itemRequestDto item's request to save and register
     * @return item's request with assigned id
     */
    @PostMapping()
    public ItemRequestOutDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("POST-request: создание запроса от пользователя c id: {}, {}", userId, itemRequestDto);
        return itemRequestService.create(userId, itemRequestDto);
    }

    /**
     * processing GET-request from a specific user to get list of the items' requests and answers to them
     *
     * @param userId requester id
     * @return list of the user's item's requests with answers to them
     */
    @GetMapping()
    public List<ItemRequestOutDto> getOwnRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
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
    @GetMapping({"all"})
    public List<ItemRequestOutDto> getOtherUsersRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @PositiveOrZero @RequestParam(
                                                                 name = "from", defaultValue = "0") Integer from,
                                                         @Positive @RequestParam(
                                                                 name = "size", defaultValue = "10") Integer size) {
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
    @GetMapping("{requestId}")
    public ItemRequestOutDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @PathVariable Long requestId) {
        log.info("GET-request: запрос на получение информации от пользователя {} о запросе с id {}", userId, requestId);
        return itemRequestService.getRequestById(userId, requestId);
    }
}
