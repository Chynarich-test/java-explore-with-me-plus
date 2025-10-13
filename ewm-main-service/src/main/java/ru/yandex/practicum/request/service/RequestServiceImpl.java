package ru.yandex.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.common.EntityValidator;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.EventState;
import ru.yandex.practicum.event.dao.EventRepository;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.request.dto.*;
import ru.yandex.practicum.request.mapper.RequestMapper;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.RequestStatus;
import ru.yandex.practicum.request.repository.RequestRepository;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper mapper;
    private final EntityValidator entityValidator;

    @Override
    public List<RequestDto> getUserRequests(Long userId) {
        entityValidator.ensureExists(userRepository, userId, "Пользователь");
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return mapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public RequestDto createRequest(Long userId, Long eventId) {
        User user = entityValidator.ensureExists(userRepository, userId, "Пользователь");
        Event event = entityValidator.ensureExists(eventRepository, eventId, "Событие");

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Инициатор события не может создать заявку на участие в своём же событии");
        }

        if (event.getState() == null || event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("Нельзя добавить заявку: событие не опубликовано");
        }

        long confirmed = requestRepository.countConfirmedRequests(eventId);
        if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0
                && confirmed >= event.getParticipantLimit()) {
            throw new ValidationException("Достигнут лимит участников события");
        }

        RequestStatus initialStatus = RequestStatus.PENDING;
        if (!event.isRequestModeration()) {
            initialStatus = RequestStatus.CONFIRMED;
        }

        Request request = mapper.toNewEntity(event, user, initialStatus);
        Request saved = requestRepository.save(request);

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) {
        entityValidator.ensureExists(userRepository, userId, "Пользователь");
        Request request = entityValidator.ensureExists(requestRepository, requestId, "Заявка");

        if (!Objects.equals(request.getRequester().getId(), userId)) {
            throw new ValidationException("Пользователь может отменять только свои заявки");
        }

        request.setStatus(RequestStatus.CANCELED);
        Request saved = requestRepository.save(request);
        return mapper.toDto(saved);
    }

    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        entityValidator.ensureExists(userRepository, userId, "Пользователь");
        Event event = entityValidator.ensureExists(eventRepository, eventId, "Событие");

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException("Только инициатор может просматривать заявки данного события");
        }

        List<Request> requests = requestRepository.findAllByEventIdWithRelations(eventId);
        return mapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        entityValidator.ensureExists(userRepository, userId, "Пользователь");
        Event event = entityValidator.ensureExists(eventRepository, eventId, "Событие");

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Только инициатор может менять статусы заявок");
        }

        String statusStr = Optional.ofNullable(updateRequest.getStatus()).orElse("").toUpperCase(Locale.ROOT);
        RequestStatus targetStatus;
        try {
            targetStatus = RequestStatus.valueOf(statusStr);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Недопустимый статус: " + updateRequest.getStatus());
        }
        if (!(targetStatus == RequestStatus.CONFIRMED || targetStatus == RequestStatus.REJECTED)) {
            throw new ValidationException("Можно массово устанавливать только CONFIRMED или REJECTED");
        }

        List<Long> ids = Optional.ofNullable(updateRequest.getRequestIds())
                .orElse(Collections.emptyList());

        if (ids.isEmpty()) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(Collections.emptyList())
                    .rejectedRequests(Collections.emptyList())
                    .build();
        }

        List<Request> requests = requestRepository.findAllByEventIdAndIds(eventId, ids);

        long confirmedNow = requestRepository.countConfirmedRequests(eventId);
        Integer limit = event.getParticipantLimit();

        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        if (targetStatus == RequestStatus.CONFIRMED) {
            for (Request req : requests) {
                if (req.getStatus() != RequestStatus.PENDING) {
                    continue;
                }
                if (limit == null || limit == 0 || confirmedNow < limit) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmedNow++;
                    confirmed.add(req);
                } else {
                    req.setStatus(RequestStatus.REJECTED);
                    rejected.add(req);
                }
            }
        } else {
            for (Request req : requests) {
                if (req.getStatus() == RequestStatus.PENDING) {
                    req.setStatus(RequestStatus.REJECTED);
                    rejected.add(req);
                }
            }
        }

        requestRepository.saveAll(requests);

        List<RequestDto> confirmedDto = mapper.toDtoList(confirmed);
        List<RequestDto> rejectedDto = mapper.toDtoList(rejected);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedDto)
                .rejectedRequests(rejectedDto)
                .build();
    }
}