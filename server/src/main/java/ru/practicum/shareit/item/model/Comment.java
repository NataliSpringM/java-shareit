package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder(toBuilder = true)
@Table(name = "comments")
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "text", nullable = false)
    private String text;
    @ManyToOne()
    @JoinColumn(name = "author", referencedColumnName = "id", nullable = false)
    private User author;
    @ManyToOne()
    @JoinColumn(name = "item", referencedColumnName = "id", nullable = false)
    private Item item;
    private LocalDateTime created;

}
