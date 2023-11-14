package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

/**
 * ItemRequest repository
 */
@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    /**
     * to get list of the items' requests from a specific user
     *
     * @param requesterId requester id
     * @return list of the item's requests with answers to them
     */
    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(Long requesterId);

    /**
     * to get list of the other users' item's requests to answer
     * list should be started with the newest requests
     * with paging option: size of the page is defined by from/to parameters of request
     *
     * @param userId      requester id
     * @param pageRequest page to get
     * @return list of the other users' item's requests
     */
    List<ItemRequest> findAllByRequesterIdIsNotOrderByCreatedDesc(Long userId, Pageable pageRequest);
}
