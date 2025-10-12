package ru.yandex.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.NewEventDto;
import ru.yandex.practicum.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.event.dto.request.UserEventsQuery;
import ru.yandex.practicum.event.service.EventService;

import java.util.List;

@Controller
@RequestMapping(path = "/users/{userId}/events")
@Slf4j
@Validated
@AllArgsConstructor
public class PrivateEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> findUserEvents(@PathVariable long userId,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        UserEventsQuery query = new UserEventsQuery(userId, from, size);
        return eventService.findEvents(query);
    }

    @PostMapping
    public EventShortDto createEvent(@PathVariable long userId,
                                     @Valid @RequestBody NewEventDto eventDto) {
        return eventService.createEvent(userId, eventDto);
    }

    @GetMapping("/{eventId}")
    public EventShortDto findUserEventById(@PathVariable long userId,
                                           @PathVariable long eventId) {
        return eventService.findUserEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody @Valid UpdateEventUserRequest updateRequest) {
        return eventService.updateUserEvent(userId, eventId, updateRequest);
    }

}
