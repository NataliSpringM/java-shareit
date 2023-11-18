package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentOutDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private ItemService itemService;

    Long itemId;
    Long userId;
    Long requestId;
    Long commentId;
    String header;
    MediaType jsonType;


    @BeforeEach
    @SneakyThrows
    void before() {
        itemId = 1L;
        userId = 1L;
        requestId = 1L;
        header = "X-Sharer-User-Id";
        jsonType = MediaType.APPLICATION_JSON;
    }

    /**
     * test create method
     * POST-request "/items"
     * when item's data are valid
     * should return status ok
     * should invoke service create method and return result
     */

    @Test
    @SneakyThrows
    public void create_WhenItemIsValid_StatusIsOk_andInvokeService() {

        //create Item with valid fields
        ItemDto validItem = ItemDto.builder()
                .id(itemId)
                .name("bike")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();

        // map item into String
        String expectedItemString = objectMapper.writeValueAsString(validItem);

        //mock service answer
        when(itemService.create(userId, validItem)).thenReturn(validItem);

        //perform request and check status and content
        String result = mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(expectedItemString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(validItem.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(validItem.getName())))
                .andExpect(jsonPath("$.description", is(validItem.getDescription())))
                .andExpect(jsonPath("$.available", is(validItem.getAvailable())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(itemService).create(userId, validItem);

        //check result
        assertEquals(expectedItemString, result);

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
        ItemOutDto item = ItemMapper.toItemOutDto(itemDto);

        // map item into String
        String expectedItemString = objectMapper.writeValueAsString(item);

        //mock service answer
        when(itemService.getById(userId, itemId)).thenReturn(item);

        //perform request and check status and content
        String result = mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedItemString))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(itemService).getById(userId, itemId);

        //check result
        assertEquals(result, expectedItemString);
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

        //mock service answer
        when(itemService.update(userId, validItem, itemId)).thenReturn(validItem);

        //perform request and check status and content
        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(expectedItemString))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(itemService).update(userId, validItem, itemId);

        //check result
        assertEquals(expectedItemString, result);
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
        mockMvc.perform(delete("/items/{itemId}", itemId))
                .andExpect(status().isOk());

        // verify invokes
        verify(itemService).deleteById(itemId);

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

        //create ItemDto objects
        ItemDto validItem1 = ItemDto.builder()
                .id(itemId)
                .name("bike")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();
        ItemDto validItem2 = ItemDto.builder()
                .id(itemId)
                .name("bike2")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();

        //create list of items
        List<ItemDto> list = List.of(validItem1, validItem2);
        List<ItemOutDto> items = list.stream().map(ItemMapper::toItemOutDto).collect(Collectors.toList());

        // map ItemOutDto list into String
        String expectedItemsListString = objectMapper.writeValueAsString(items);

        //mock service answer
        when(itemService.getListByUser(userId)).thenReturn(items);

        //perform request and check status and content
        String result = mockMvc.perform(get("/items")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].id", is(validItem1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(validItem1.getName()), String.class))
                .andExpect(jsonPath("$.[1].name", is(validItem2.getName()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(itemService).getListByUser(userId);

        //check result
        assertEquals(result, expectedItemsListString);
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
        String parameterName = "text";
        String parameterValue = "substring";

        //create ItemDto objects
        ItemDto validItem1 = ItemDto.builder()
                .id(itemId)
                .name("bike")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();
        ItemDto validItem2 = ItemDto.builder()
                .id(itemId)
                .name("bike2")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();

        //create list of items
        List<ItemDto> list = List.of(validItem1, validItem2);
        List<ItemOutDto> items = list.stream().map(ItemMapper::toItemOutDto).collect(Collectors.toList());

        // map list into String
        String itemsString = objectMapper.writeValueAsString(items);

        //mock service answer
        when(itemService.searchItemsBySubstring(parameterValue)).thenReturn(items);

        //perform request and check status and content
        String result = mockMvc.perform(get("/items/search")
                        .param(parameterName, parameterValue))
                .andExpect(status().isOk())
                .andExpect(content().json(itemsString))
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(itemService).searchItemsBySubstring(parameterValue);

        //check result
        assertEquals(result, itemsString);
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
        CommentOutDto commentOut = CommentMapper.toCommentOutDto(comment);

        // map comment into String
        String expectedCommentString = objectMapper.writeValueAsString(commentOut);

        //mock service answer
        when(itemService.addComment(comment, userId, itemId)).thenReturn(commentOut);

        //perform request and check status and content
        String result = mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .content(objectMapper.writeValueAsString(comment))
                        .contentType(jsonType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentOut.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentOut.getText())))
                .andExpect(jsonPath("$.authorName", is(commentOut.getAuthorName())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(itemService).addComment(comment, userId, itemId);

        //check result
        assertEquals(result, expectedCommentString);
    }

}
