package ru.yandex.practicum.event.dto;

import lombok.Data;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.event.model.EventState;
import ru.yandex.practicum.event.model.Location;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventFullDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private long confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    private String eventDate;
    private UserShortDto initiator;
    private Location location;
    private boolean paid;
    private int participantLimit;
    private LocalDateTime publishedOn;
    private boolean requestModeration;
    private EventState state;
    private String title;
    private long views;

    // Новые поля (опционально, так как не знаю как поведут себя тесты постмана)
    private long commentsCount;
    private List<CommentDto> latestComments;
}

