package ru.yandex.practicum.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.event.model.Event;

@Builder
@Entity
@Table(name = "requests", schema = "public")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

}
