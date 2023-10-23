package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

/**
 * TODO Sprint add-controllers.
 * Item model.
 */
@Data
@Builder(toBuilder = true)
@Entity
@Table(name = "items")
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "description", nullable = false)
    String description;
    @Column(name = "available", nullable = false)
    Boolean available;
    @ManyToOne()
    @JoinColumn(name = "owner", referencedColumnName = "id", nullable = false)
    User owner;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request", referencedColumnName = "id")
    ItemRequest request;

}