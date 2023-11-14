package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.constants.Constants.*;

/**
 * ItemController tests
 */

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemClient itemClient;

    Long itemId;
    Long userId;
    Long requestId;
    Long commentId;

    @BeforeEach
    @SneakyThrows
    void before() {
        itemId = 1L;
        userId = 1L;
        requestId = 1L;
        commentId = 1L;
    }

    /**
     * test addItem method
     * POST-request "/items"
     * when item's data are valid
     * should return status ok
     * should invoke service addItem method
     */
    @Test
    @SneakyThrows
    public void addItem_WhenItemIsValid_StatusIsOk_andInvokeService() {

        //create Item with valid fields
        ItemDto validItem = ItemDto.builder()
                .id(itemId)
                .name("bike")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();

        // map item into String
        String itemString = objectMapper.writeValueAsString(validItem);

        //perform request and check status
        mockMvc.perform(post(ITEMS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemString))
                .andExpect(status().isOk());

        // verify invokes
        verify(itemClient).addItem(eq(userId), any(ItemDto.class));

    }

    /**
     * test addItem method
     * POST-request "/items"
     * when item's data are not valid: has empty name
     * should return status bad request
     * should not invoke service addItem method
     */

    @Test
    @SneakyThrows
    public void addItem_whenItemHasEmptyName_statusIsBadRequest_doesNotInvokeService() {

        //addItem invalid field
        String emptyName = "";

        //create Item with invalid fields
        ItemDto invalidItem = ItemDto.builder()
                .id(itemId)
                .name(emptyName)
                .description("description")
                .available(true)
                .requestId(1L)
                .build();

        // map item into String
        String invalidItemString = objectMapper.writeValueAsString(invalidItem);

        //perform tested request and check status
        mockMvc.perform(post(ITEMS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidItemString))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(itemClient, Mockito.never()).addItem(anyLong(), any());
    }

    /**
     * test addItem method
     * POST-request "/items"
     * when item's data are not valid: has empty description
     * should return status bad request
     * should not invoke service addItem method
     */
    @Test
    @SneakyThrows
    public void addItem_whenItemHasEmptyDescription_statusIsBadRequest_doesNotInvokeService() {

        //create invalid field
        String emptyDescription = "";

        //create Item with invalid fields
        ItemDto invalidItem = ItemDto.builder()
                .id(itemId)
                .name("bike")
                .description(emptyDescription)
                .available(true)
                .requestId(1L)
                .build();

        // map item into String
        String invalidItemString = objectMapper.writeValueAsString(invalidItem);

        //perform tested request and check status
        mockMvc.perform(post(ITEMS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidItemString))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(itemClient, Mockito.never()).addItem(anyLong(), any());
    }

    /**
     * test addItem method
     * POST-request "/items"
     * when item's data are not valid: name is null
     * should return status bad request
     * should not invoke service addItem method
     */
    @Test
    @SneakyThrows
    public void addItem_whenItemHasNullName_statusIsBadRequest_doesNotInvokeService() {

        //create Item with null name
        ItemDto invalidItem = ItemDto.builder()
                .id(itemId)
                .name(null)
                .description("description")
                .available(true)
                .requestId(1L)
                .build();

        // map item into String
        String invalidItemString = objectMapper.writeValueAsString(invalidItem);

        //perform tested request and check status
        mockMvc.perform(post(ITEMS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidItemString))
                .andExpect(status().isBadRequest());


        // verify invokes
        verify(itemClient, Mockito.never()).addItem(anyLong(), any());
    }

    /**
     * test addItem method
     * POST-request "/items"
     * when item's data are not valid: description is null
     * should return status bad request
     * should not invoke service addItem method
     */

    @Test
    @SneakyThrows
    public void addItem_whenItemHasNullDescription_wtatusIsBadRequest_doesNotInvokeService() {

        //create Item with null description
        ItemDto invalidItem = ItemDto.builder()
                .id(itemId)
                .name("bike")
                .description(null)
                .available(true)
                .requestId(1L)
                .build();

        // map item into String
        String invalidItemString = objectMapper.writeValueAsString(invalidItem);

        //perform tested request and check status
        mockMvc.perform(post(ITEMS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidItemString))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(itemClient, Mockito.never()).addItem(anyLong(), any());
    }

    /**
     * test addItem method
     * POST-request "/items"
     * when item's data are not valid: available is null
     * should return status bad request
     * should not invoke service addItem method
     */

    @Test
    @SneakyThrows
    public void addItem_whenItemHasNullAvailable_statusIsBadRequest_doesNotInvokeService() {

        //create Item with null available
        ItemDto invalidItem = ItemDto.builder()
                .id(itemId)
                .name("bike")
                .description("description")
                .available(null)
                .requestId(1L)
                .build();

        // map item into String
        String invalidItemString = objectMapper.writeValueAsString(invalidItem);

        //perform tested request and check status
        mockMvc.perform(post(ITEMS_PATH)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidItemString))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(itemClient, Mockito.never()).addItem(anyLong(), any());
    }

    /**
     * test getById method
     * GET-request "/items/{itemId}"
     * should return status ok
     * should invoke service getById method and return result
     */

    @SneakyThrows
    @Test
    public void getById_statusIsOk_andInvokeService() {

        //create Item with valid fields
        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .name("Item")
                .description("description")
                .available(true)
                .requestId(1L)
                .build();

        // map item into String
        String itemString = objectMapper.writeValueAsString(itemDto);

        //perform request and check status
        mockMvc.perform(get(ITEMS_PATH + ITEM_ID_PATH_VARIABLE, itemId)
                        .header(HEADER_USER_ID, userId))
                .andExpect(status().isOk());

        // verify invokes
        verify(itemClient).getItemById(userId, itemId);

    }

    /**
     * test update method
     * PATCH-request "/items/{itemId}"
     * should return status ok
     * should invoke service update method and return result
     */
    @Test
    @SneakyThrows
    public void update_statusIsOk_andInvokeService() {

        //create ItemDto with valid fields to update
        ItemDto validItem = ItemDto.builder()
                .id(itemId)
                .name("bike")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();

        // map item into String
        String expectedItemString = objectMapper.writeValueAsString(validItem);

        //perform request and check status
        mockMvc.perform(patch(ITEMS_PATH + ITEM_ID_PATH_VARIABLE, itemId)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expectedItemString))
                .andExpect(status().isOk());

        // verify invokes
        verify(itemClient).updateItem(eq(userId), any(ItemDto.class), eq(itemId));
    }

    /**
     * test delete method
     * DELETE-request "/items/{itemId}"
     * should return status ok
     * should invoke service deleteById method
     */
    @Test
    @SneakyThrows
    public void delete_statusIsOk_andInvokeService() {

        //perform request and check status
        mockMvc.perform(delete(ITEMS_PATH + ITEM_ID_PATH_VARIABLE, itemId))
                .andExpect(status().isOk());

        // verify invokes
        verify(itemClient).deleteItemById(itemId);

    }

    /**
     * test getListByUser method
     * GET-request "/items"
     * should return status ok
     * should invoke service getListByUser method and return result
     */
    @Test
    @SneakyThrows
    public void getListByUser_isStatusOk_andInvokeService() {

        //perform request and check status and content
        mockMvc.perform(get(ITEMS_PATH)
                        .header(HEADER_USER_ID, userId))
                .andExpect(status().isOk());

        // verify invokes
        verify(itemClient).getListByUserId(userId);
    }

    /**
     * test searchItemsBySubstring method
     * GET-request "/items/search"
     * should return status ok
     * should invoke service searchItemsBySubstring method and return result
     */

    @Test
    @SneakyThrows
    public void searchItemsBySubstring_isStatusOk_andInvokeService() {

        //create valid parameters for search
        String parameterValue = "substring";

        //perform request and check status and content
        mockMvc.perform(get(ITEMS_PATH + SEARCH_PATH)
                        .param(TEXT_PARAMETER_NAME, parameterValue))
                .andExpect(status().isOk());

        // verify invokes
        verify(itemClient).searchItemsBySubstring(parameterValue);

    }

    /**
     * test searchItemsBySubstring method
     * GET-request "/items/search"
     * has required parameter "text"
     * when parameter name is not valid
     * should return status internal server error
     * should not invoke service searchItemsBySubstring method
     */

    @Test
    @SneakyThrows
    public void searchItemsBySubstring_whenParameterNameIsInvalid_isStatusInternalServerError_AndInvokeService() {

        //create invalid parameter
        String parameterName = "invalid";

        //create valid parameter fo search
        String parameterValue = "substring";

        //perform request and check status
        mockMvc.perform(get(ITEMS_PATH + SEARCH_PATH)
                        .param(parameterName, parameterValue))
                .andExpect(status().isInternalServerError());

        // verify invokes
        verify(itemClient, never()).searchItemsBySubstring(anyString());
    }

    /**
     * test addComment method
     * GET-request "/items/{itemId}/comment"
     * when comment's data are valid
     * should return status ok
     * should invoke service addComment method and return result
     */

    @Test
    @SneakyThrows
    public void addComment_whenValidComment_isStatusOk_andInvokeService() {

        //create comment with valid fields
        CommentDto comment = CommentDto.builder()
                .id(commentId)
                .text("commentText")
                .authorName("Olga")
                .itemId(itemId)
                .build();

        //perform request and check status and content
        mockMvc.perform(post(ITEMS_PATH + COMMENT_PATH_VARIABLE, itemId)
                        .header(HEADER_USER_ID, userId)
                        .content(objectMapper.writeValueAsString(comment))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


        // verify invokes
        verify(itemClient).addComment(any(CommentDto.class), eq(userId), eq(itemId));

    }

    /**
     * test addComment method
     * GET-request "/items/{itemId}/comment"
     * when comment's data are not valid: text is empty
     * should return status bad request
     * should not invoke service addComment method
     */

    @Test
    @SneakyThrows
    public void addComment_whenInvalidCommentEmptyText_isStatusBadRequest_andDoesNotInvokeService() {

        //create invalid field
        String emptyText = "";

        //create invalidCommentDto object
        CommentDto invalidComment = CommentDto.builder()
                .id(commentId)
                .text(emptyText)
                .authorName("Olga")
                .itemId(itemId)
                .build();

        //map Comment into String
        String invalidCommentString = objectMapper.writeValueAsString(invalidComment);

        //perform tested request and check status
        mockMvc.perform(post(ITEMS_PATH + COMMENT_PATH_VARIABLE, itemId)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCommentString))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(itemClient, never()).addComment(invalidComment, userId, itemId);

    }

    /**
     * test addComment method
     * GET-request "/items/{itemId}/comment"
     * when comment's data are not valid: text is null
     * should return status bad request
     * should not invoke service addComment method
     */

    @Test
    @SneakyThrows
    public void addComment_whenInvalidCommentNullText_isStatusBadRequest_andDoesNotInvokeService() {

        //create invalidCommentDto object with null text
        CommentDto invalidComment = CommentDto.builder()
                .id(commentId)
                .text(null)
                .authorName("Olga")
                .itemId(itemId)
                .build();

        //map Comment into String
        String invalidCommentString = objectMapper.writeValueAsString(invalidComment);

        //perform tested request and check status
        mockMvc.perform(post(ITEMS_PATH + COMMENT_PATH_VARIABLE, itemId)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCommentString))
                .andExpect(status().isBadRequest());

        // verify invokes
        verify(itemClient, never()).addComment(any(), anyLong(), anyLong());
    }
}
