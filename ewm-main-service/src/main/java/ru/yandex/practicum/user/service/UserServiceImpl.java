package ru.yandex.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.common.EntityValidator;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.user.dto.NewUserRequest;
import ru.yandex.practicum.user.dto.PageParams;
import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.mapper.UserMapper;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EntityValidator entityValidator;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        validateNewUserRequest(request);
        ensureEmailUnique(request.getEmail(), null);

        User user = UserMapper.toEntity(request);
        setDefaultNameIfEmpty(user);

        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    @Override
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден!"));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, PageParams pageParams) {
        PageRequest pageable = PageRequest.of(pageParams.getPageNumber(), Math.max(1, pageParams.getSize()));
        List<User> users;
        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids, pageable);
        } else {
            users = userRepository.findAllBy(pageable);
        }
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        entityValidator.ensureExists(userRepository, id, "Пользователь");
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден!"));

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            ensureEmailUnique(userDto.getEmail(), id);
        }

        UserMapper.updateEntityFromDto(user, userDto);
        setDefaultNameIfEmpty(user);

        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    private void validateNewUserRequest(NewUserRequest req) {
        if (req == null) {
            throw new ValidationException("Запрос на добавление пользователя не должен быть пустым");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
    }

    private void ensureEmailUnique(String email, Long existingUserIdToIgnore) {
        userRepository.findByEmail(email).ifPresent(u -> {
            if (existingUserIdToIgnore == null || !Objects.equals(u.getId(), existingUserIdToIgnore)) {
                throw new ValidationException("Email должен быть уникальным!");
            }
        });
    }

    private void setDefaultNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            String email = user.getEmail();
            String defaultName = email;
            int at = email.indexOf("@");
            if (at > 0) defaultName = email.substring(0, at);
            user.setName(defaultName);
        }
    }
}