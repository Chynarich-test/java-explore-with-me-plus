package ru.yandex.practicum.event.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.category.mapper.CategoryMapper;
import ru.yandex.practicum.event.dto.*;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.location.mapper.LocationMapper;
import ru.yandex.practicum.user.mapper.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, LocationMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(source = "event.eventDate", target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventFullDto toEventFullDto(Event event);

    EventShortDto toEventShortDto(Event event);

    //  @Mapping(source = "category", target = "category.id")
    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    Event fromNewEventDto(NewEventDto dto);

    List<EventShortDto> toEventsShortDto(List<Event> events);

    //  @Mapping(target = "category.id", source = "category")
    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "location", ignore = true)
    void updateEventFromUserDto(UpdateEventUserRequest dto, @MappingTarget Event entity);

    // @Mapping(target = "category.id", source = "category")
    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "location", ignore = true)
    void updateEventFromAdminDto(UpdateEventAdminRequest dto, @MappingTarget Event entity);

    List<EventFullDto> toEventsFullDto(List<Event> events);

    @Named("mapCategoryIdToCategory")
    default ru.yandex.practicum.category.model.Category mapCategoryIdToCategory(Long id) {
        if (id == null) return null;
        ru.yandex.practicum.category.model.Category category = new ru.yandex.practicum.category.model.Category();
        category.setId(id);
        return category;
    }
}
