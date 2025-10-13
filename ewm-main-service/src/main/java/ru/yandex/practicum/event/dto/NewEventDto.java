package ru.yandex.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import ru.yandex.practicum.location.dto.LocationDto;

import java.time.LocalDateTime;

@Data
public class NewEventDto {
    @NotBlank(message = "Аннотация не может быть пустой")
    private String annotation;
    @Positive(message = "ID категории должен быть положительным числом")
    private long category;
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
    @NotNull(message = "Дата события обязательна")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull(message = "Местоположение обязательно")
    private LocationDto location;
    private Boolean paid;
    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    private int participantLimit;
    private Boolean requestModeration;
    @NotBlank(message = "Заголовок не может быть пустым")
    private String title;
}
