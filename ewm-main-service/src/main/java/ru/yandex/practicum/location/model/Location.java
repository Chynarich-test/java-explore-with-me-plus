package ru.yandex.practicum.location.model;


import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Table(name = "locations", schema = "public")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "lat", nullable = false)
    private float lat;
    @Column(name = "lon", nullable = false)
    private float lon;
}
