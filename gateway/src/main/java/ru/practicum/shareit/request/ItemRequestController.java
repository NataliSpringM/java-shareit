package ru.practicum.shareit.request;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * processing HTTP-requests to "/requests" end-point to add, update or get requests' data.
 */
@Slf4j
@Controller
@Validated
@RequiredArgsConstructor
@RequestMapping(path = REQUESTS_PATH)
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    /**
     * processing POST-request to add item's request (save and assign identity)
     *
     * @param userId         requester id
     * @param itemRequestDto item's request to save and register
     * @return item's request with assigned id
     */
    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader(HEADER_USER_ID) Long userId,
                                             @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("User {} added a request: {}", userId, itemRequestDto);
        return itemRequestClient.addRequest(userId, itemRequestDto);
    }

    /**
     * processing GET-request from a specific user to get list of the items' requests and answers to them
     *
     * @param userId requester id
     * @return list of the user's item's requests with answers to them
     */
    @GetMapping()
    public ResponseEntity<Object> getOwnRequests(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("User {} get own requests with items suggested in response", userId);
        return itemRequestClient.getOwnRequests(userId);
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
    public ResponseEntity<Object> getOtherUsersRequests(@RequestHeader(HEADER_USER_ID) Long userId,
                                                        @PositiveOrZero @RequestParam(
                                                                name = FROM_PARAMETER_NAME,
                                                                defaultValue = ZERO_DEFAULT_VALUE) Integer from,
                                                        @Positive @RequestParam(
                                                                name = SIZE_PARAMETER_NAME,
                                                                defaultValue = TEN_DEFAULT_VALUE) Integer size) {
        log.info("User {} get all requests, from {}, size {}", userId, from, size);
        return itemRequestClient.getOtherUsersRequests(userId, from, size);
    }

    /**
     * processing GET-request to get item's request by id
     *
     * @param requestId item's request id
     * @param userId    user's id
     * @return item's request
     */
    @GetMapping(REQUEST_ID_PATH_VARIABLE)
    public ResponseEntity<Object> getRequestById(@RequestHeader(HEADER_USER_ID) Long userId,
                                                 @PathVariable Long requestId) {
        log.info("User {} get info about request with id {}", userId, requestId);
        return itemRequestClient.getRequestById(userId, requestId);
    }
}

