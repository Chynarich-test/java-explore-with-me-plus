package ru.yandex.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.request.dto.*;
import ru.yandex.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RequestController {

    private final RequestService service;

    // Пользователь: получить свои запросы
    @GetMapping("/users/{userId}/requests")
    public List<RequestDto> getUserRequests(@PathVariable Long userId) {
        return service.getUserRequests(userId);
    }

    // Пользователь: создать запрос
    @PostMapping("/users/{userId}/requests")
    public RequestDto createRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {
        return service.createRequest(userId, eventId);
    }

    // Пользователь: отменить свой запрос
    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public RequestDto cancelRequest(@PathVariable Long userId,
                                    @PathVariable Long requestId) {
        return service.cancelRequest(userId, requestId);
    }

    // Инициатор: получить заявки на своё событие
    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<RequestDto> getEventRequests(@PathVariable Long userId,
                                             @PathVariable Long eventId) {
        return service.getEventRequests(userId, eventId);
    }

    // Инициатор: массово поменять статусы (body — EventRequestStatusUpdateRequest)
    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatuses(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request) {
        return service.changeRequestStatus(userId, eventId, request);
    }
}