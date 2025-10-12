package ru.yandex.practicum.user.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Table(name = "users", schema = "public")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "name", nullable = false)
    private String name;
}
