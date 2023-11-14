package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.util.client.BaseClient;

import java.util.Map;

import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * creation requests to "/requests" endpoint
 */
@Service
public class ItemRequestClient extends BaseClient {
    @Autowired
    public ItemRequestClient(@Value(API_SERVER_URL) String serverUrl, RestTemplateBuilder restTemplateBuilder) {
        super(
                restTemplateBuilder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + REQUESTS_PATH))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    /**
     * create POST-request to add item's request (save and assign identity)
     *
     * @param userId         requester id
     * @param itemRequestDto item's request to save and register
     * @return POST-request
     */

    public ResponseEntity<Object> addRequest(Long userId, ItemRequestDto itemRequestDto) {
        return post(EMPTY_PATH, userId, itemRequestDto);
    }

    /**
     * create GET-request from a specific user to get list of the items' requests and answers to them
     *
     * @param userId requester id
     * @return GET-request
     */

    public ResponseEntity<Object> getOwnRequests(Long userId) {
        return get(EMPTY_PATH, userId);
    }

    /**
     * create GET-request from a specific user to get list of the other users' items requests to answer
     * list should be started with the newest requests
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     *
     * @param userId requester id
     * @param from   first index of the request at the page
     * @param size   size of the page
     * @return GET-request
     */

    public ResponseEntity<Object> getOtherUsersRequests(Long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                FROM_PARAMETER_NAME, from,
                SIZE_PARAMETER_NAME, size
        );
        return get(ALL_PATH, userId, parameters);
    }

    /**
     * processing GET-request to get item's request by id
     *
     * @param requestId item's request id
     * @param userId    user's id
     * @return GET-request
     */

    public ResponseEntity<Object> getRequestById(Long userId, Long requestId) {
        return get(SLASH_PATH + requestId, userId);
    }
}
