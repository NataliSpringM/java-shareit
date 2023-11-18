package ru.practicum.shareit.item;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.util.client.BaseClient;

import java.util.Map;

import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * creation requests to "/items" endpoint
 */
@Service
public class ItemClient extends BaseClient {
    @Autowired
    public ItemClient(@Value(API_SERVER_URL) String serverUrl, RestTemplateBuilder restTemplateBuilder) {
        super(
                restTemplateBuilder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + ITEMS_PATH))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    /**
     * create POST-request to add item's data
     *
     * @param userId  owner's id
     * @param itemDto item to save and register
     * @return POST-request
     */
    public ResponseEntity<Object> addItem(Long userId, ItemDto itemDto) {
        return post(EMPTY_PATH, userId, itemDto);
    }

    /**
     * create GET-request to get item by id
     *
     * @param userId user's id
     * @param itemId item's id
     * @return GET-request
     */

    public ResponseEntity<Object> getItemById(Long userId, Long itemId) {
        return get(SLASH_PATH + itemId, userId);
    }

    /**
     * create PATCH-request to update item's properties
     *
     * @param userId  owner's id
     * @param itemId  item's id
     * @param itemDto object with properties to update
     * @return PATCH-request
     */
    public ResponseEntity<Object> updateItem(Long userId, ItemDto itemDto, Long itemId) {
        return patch(SLASH_PATH + itemId, userId, itemDto);
    }

    /**
     * create DELETE-request to delete item
     *
     * @param itemId item's id
     */
    public void deleteItemById(Long itemId) {
        delete(SLASH_PATH + itemId);
    }

    /**
     * create GET-request to get all items of a specific user
     *
     * @param userId user's id
     * @return GET-request
     */

    public ResponseEntity<Object> getListByUserId(Long userId) {
        return get(EMPTY_PATH, userId);
    }

    /**
     * create GET-request to search all available items, contained substring in name or description
     *
     * @param substring substring for search
     * @return GET-request
     */

    public ResponseEntity<Object> searchItemsBySubstring(String substring) {
        Map<String, Object> parameters = Map.of(
                TEXT_PARAMETER_NAME, substring
        );
        return get(SEARCH_PATH + constructSearchParametersPath(substring), parameters);
    }

    /**
     * create POST-request to add comment to a specific item
     *
     * @param commentDto comment
     * @param userId     author's id
     * @param itemId     item's id
     * @return POST-request
     */

    public ResponseEntity<Object> addComment(CommentDto commentDto, Long userId, Long itemId) {
        return post(SLASH_PATH + itemId + COMMENT_PATH, userId, commentDto);
    }

    /**
     * construct path with parameters for GET-request
     *
     * @param substring substring to search
     * @return path with list of parameters as name=value pairs
     */

    private String constructSearchParametersPath(String substring) {
        return "?"
                + constructParamPair(TEXT_PARAMETER_NAME, substring);
    }


    /**
     * construct String with name={value} pair
     */
    private String constructParamPair(String name, Object value) {
        return name
                + "="
                + value;
    }
}
