package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;

import java.util.List;

/**
 * ItemRequestService interface
 */

@Component
public interface ItemRequestService {

    /**
     * to add item's request (save and assign identity)
     *
     * @param userId         owner's id
     * @param itemRequestDto item's request to save and register
     * @return item's request with assigned id
     */
    ItemRequestOutDto create(Long userId, ItemRequestDto itemRequestDto);

    /**
     * to get list of the items' requests and answers to them from a specific user
     *
     * @param userId requester id
     * @return list of the item's requests with answers to them
     */
    List<ItemRequestOutDto> getOwnRequests(Long userId);

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
    List<ItemRequestOutDto> getOtherUsersRequests(Long userId, Integer from, Integer size);

    /**
     * to get item's request by id
     *
     * @param requestId item's request id
     * @param userId    user's id
     * @return item's request
     */
    ItemRequestOutDto getRequestById(Long userId, Long requestId);
}
