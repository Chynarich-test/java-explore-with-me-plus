package ru.yandex.practicum.user.mapper;

import ru.yandex.practicum.user.dto.NewUserRequest;
import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.model.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static User toEntity(NewUserRequest req) {
        if (req == null) return null;
        return User.builder()
                .email(req.getEmail())
                .name(req.getName())
                .build();
    }

    public static void updateEntityFromDto(User user, UserDto dto) {
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getName() != null) user.setName(dto.getName());
    }
}