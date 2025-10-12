package ru.yandex.practicum.location.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class LocationDto {
    @NotNull(message = "Широта (lat) не может быть null")
    @DecimalMin(value = "-90.0", message = "Широта (lat) не может быть меньше -90.0")
    @DecimalMax(value = "90.0", message = "Широта (lat) не может быть больше 90.0")
    private float lat;
    @NotNull(message = "Долгота (lon) не может быть null")
    @DecimalMin(value = "-180.0", message = "Долгота (lon) не может быть меньше -180.0")
    @DecimalMax(value = "180.0", message = "Долгота (lon) не может быть больше 180.0")
    private float lon;
}
