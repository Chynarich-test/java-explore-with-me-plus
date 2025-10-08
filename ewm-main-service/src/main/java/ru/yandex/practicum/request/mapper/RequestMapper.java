package ru.yandex.practicum.request.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.request.dto.RequestDto;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.RequestStatus;
import ru.yandex.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequestMapper {

    public RequestDto toDto(Request pr) {
        return RequestDto.builder()
                .id(pr.getId())
                .created(pr.getCreated())
                .event(pr.getEvent().getId())
                .requester(pr.getRequester().getId())
                .status(pr.getStatus().name())
                .build();
    }

    public List<RequestDto> toDtoList(List<Request> list) {
        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Request toNewEntity(Event event, User requester, RequestStatus status) {
        return Request.builder()
                .event(event)
                .requester(requester)
                .created(LocalDateTime.now())
                .status(status)
                .build();
    }
}