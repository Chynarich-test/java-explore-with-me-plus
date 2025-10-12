package ru.yandex.practicum.user.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.user.dto.UserShortDto;
import ru.yandex.practicum.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserShortDto toUserShortDto(User user);

    User toUser(UserShortDto userShortDto);
}
