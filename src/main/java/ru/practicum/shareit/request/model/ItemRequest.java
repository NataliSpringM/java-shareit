package ru.practicum.shareit.request.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Sprint add-item-requests.
 * ItemRequest model.
 */
@Data
@Builder(toBuilder = true)
@Entity
@Table(name = "requests")
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "description", nullable = false)
    private String description;
    @ManyToOne
    @JoinColumn(name = "requester", referencedColumnName = "id")
    private User requester;
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

}