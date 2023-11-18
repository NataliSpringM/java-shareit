package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * ItemRepository tests
 */
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRepositoryTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    ItemRequestRepository itemRequestRepository;
    User owner;
    Long ownerId;
    User requester;
    Long requesterId;
    Item item1;
    Item item2;
    ItemRequest item1Request;
    ItemRequest item2Request;
    Long item1RequestId;
    Long item2RequestId;

    /**
     * create records in database to test
     */
    @BeforeEach
    public void beforeEach() {
        owner = User.builder()
                .name("Olga")
                .email("Olga@yandex.ru")
                .build();
        owner = userRepository.save(owner);
        ownerId = owner.getId();
        requester = User.builder()
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();
        requester = userRepository.save(requester);
        requesterId = requester.getId();
        item1Request = ItemRequest.builder()
                .description("I need bike")
                .requester(requester)
                .created(LocalDateTime.of(2023, 1, 1, 1, 1, 1))
                .build();
        item2Request = ItemRequest.builder()
                .description("I need pram")
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
        itemRequestRepository.save(item1Request);
        item1RequestId = item1Request.getId();
        itemRequestRepository.save(item2Request);
        item2RequestId = item2Request.getId();
        item1 = Item.builder()
                .name("bike")
                .description("old")
                .available(true)
                .owner(owner)
                .request(item1Request)
                .build();
        item1 = itemRepository.save(item1);
        item2 = Item.builder()
                .name("pram")
                .description("nEw")
                .available(true)
                .owner(owner)
                .request(item2Request)
                .build();
        item2 = itemRepository.save(item2);


    }

    /**
     * should find all items of a specific user by user's id
     */
    @Test
    public void findAllByOwnerId() {

        List<Item> result = itemRepository.findAllByOwnerIdOrderById(ownerId);

        assertThat(result).asList()
                .hasSize(2)
                .contains(item1)
                .contains(item2);
    }

    /**
     * search all available items, contained substring in name or description
     */
    @Test
    public void searchItemsBySubstring() {

        List<Item> resultTwoItems = itemRepository.searchItemsBySubstring("e");

        assertThat(resultTwoItems).asList()
                .hasSize(2)
                .contains(item1)
                .contains(item2);

        List<Item> resultOneItem = itemRepository.searchItemsBySubstring("prAm");

        assertThat(resultOneItem).asList()
                .hasSize(1)
                .doesNotContain(item1)
                .contains(item2);

        List<Item> resultNoItem = itemRepository.searchItemsBySubstring("willow");
        assertThat(resultNoItem).asList()
                .isEmpty();
    }

    /**
     * should find list of items by request's id
     */
    @Test
    public void findAllByRequestId() {

        List<Item> result2 = itemRepository.findAllByRequestId(item2RequestId);

        assertThat(result2).asList()
                .hasSize(1)
                .contains(item2)
                .doesNotContain(item1);

        List<Item> result = itemRepository.findAllByRequestId(item1RequestId);

        assertThat(result).asList()
                .hasSize(1)
                .contains(item1)
                .doesNotContain(item2);

    }

    /**
     * should find list of items by requests' list
     */
    @Test
    public void findAllByRequestIn() {

        List<ItemRequest> itemRequests = List.of(item1Request, item2Request);

        List<Item> result = itemRepository.findAllByRequestIn(itemRequests);

        assertThat(result).asList()
                .hasSize(2)
                .contains(item1)
                .contains(item2);

    }

}
