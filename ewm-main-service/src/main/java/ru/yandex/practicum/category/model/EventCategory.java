package ru.yandex.practicum.category.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Table(name = "categories", schema = "public")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
}
