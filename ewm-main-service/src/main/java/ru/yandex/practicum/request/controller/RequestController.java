package ru.yandex.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.request.dto.*;
import ru.yandex.practicum.request.service.RequestServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class RequestController {

    private final RequestServiceImpl service;

    @GetMapping("/requests")
    public List<RequestDto> getUserRequests(@PathVariable Long userId) {
        return service.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createRequest(@PathVariable Long userId,
                                    @RequestParam Long eventId) {
        return service.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public RequestDto cancelRequest(@PathVariable Long userId,
                                    @PathVariable Long requestId) {
        return service.cancelRequest(userId, requestId);
    }
}