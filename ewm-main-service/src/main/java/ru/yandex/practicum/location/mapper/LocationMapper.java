package ru.yandex.practicum.location.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.location.dto.LocationDto;
import ru.yandex.practicum.location.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location toLocation(LocationDto locationDto);

    LocationDto toLocationDto(Location location);
}
