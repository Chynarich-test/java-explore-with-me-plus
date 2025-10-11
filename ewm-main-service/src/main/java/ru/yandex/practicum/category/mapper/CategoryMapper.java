package ru.yandex.practicum.category.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.model.EventCategory;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toCategoryDto(EventCategory eventCategory);

    EventCategory toEventCategory(CategoryDto categoryDto);
}
