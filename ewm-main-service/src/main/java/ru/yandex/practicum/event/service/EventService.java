package ru.yandex.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.event.dao.EventRepository;
import ru.yandex.practicum.event.dto.*;
import ru.yandex.practicum.event.dto.enums.AdminStateAction;
import ru.yandex.practicum.event.dto.enums.EventSort;
import ru.yandex.practicum.event.dto.enums.UserStateAction;
import ru.yandex.practicum.event.dto.request.AdminEventFilter;
import ru.yandex.practicum.event.dto.request.PublicEventFilter;
import ru.yandex.practicum.event.dto.request.UserEventsQuery;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.EventState;
import ru.yandex.practicum.exception.ExistException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.user.dao.UserRepository;
import ru.yandex.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class EventService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final StatsClient statsClient;
    private final HttpServletRequest request;

    public List<EventShortDto> findEvents(UserEventsQuery query) {
        List<EventShortDto> dtos = eventMapper.toEventsShortDto(eventRepository.findByInitiatorId(query.userId(),
                PageRequest.of(query.from() / query.size(), query.size())));

        if (dtos != null && !dtos.isEmpty()) {
            var uris = dtos.stream().map(d -> "/events/" + d.getId()).collect(Collectors.toList());
            var hits = fetchHitsForUris(uris);
            for (EventShortDto dto : dtos) {
                dto.setViews(hits.getOrDefault("/events/" + dto.getId(), 0L));
            }
        }

        return dtos;
    }

    @Transactional
    public EventShortDto createEvent(long userId, NewEventDto eventDto) {
        User owner = findById(userRepository, userId, "User");

        if (eventDto.getEventDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (eventDto.getEventDate().isBefore(now.plusHours(2))) {
                throw new ExistException("Event date must be at least 2 hours in the future");
            }
        }

        Event event = eventMapper.fromNewEventDto(eventDto);
        event.setInitiator(owner);

        Event savedItem = eventRepository.save(event);
        return eventMapper.toEventShortDto(savedItem);
    }

    public EventShortDto findUserEventById(long userId, long eventId) {
        EventShortDto dto = eventMapper.toEventShortDto(findByIdAndUser(userId, eventId));
        if (dto != null) {
            var uri = "/events/" + dto.getId();
            var hits = fetchHitsForUris(List.of(uri));
            dto.setViews(hits.getOrDefault(uri, 0L));
        }
        return dto;
    }

    private <T, E extends JpaRepository<T, Long>> T findById(E repo, long id, String startWord) {
        return repo.findById(id).orElseThrow(() ->
                new NotFoundException(startWord + " with id=" + id + " was not found"));
    }

    private Event findByPublicId(long eventId) {
        return eventRepository.findByIdAndState(eventId, EventState.PUBLISHED).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Event findByIdAndUser(long userId, long eventId) {
        return eventRepository.findByIdAndInitiatorId(userId, eventId).orElseThrow(() ->
                new NotFoundException("Владелец с ID " + userId + " или ивент с ID " + eventId + " не найдены"));
    }

    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = findByIdAndUser(userId, eventId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ExistException("Only pending or canceled events can be changed");
        }

        if (updateRequest.getEventDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (updateRequest.getEventDate().isBefore(now.plusHours(2))) {
                throw new ExistException("Event date must be at least 2 hours in the future");
            }
        }

        eventMapper.updateEventFromUserDto(updateRequest, event);

        if (updateRequest.getStateAction() != null) {
            if (event.getState().equals(EventState.CANCELED) &&
                    updateRequest.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else if (event.getState().equals(EventState.PENDING) &&
                    updateRequest.getStateAction().equals(UserStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }

        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    public List<EventShortDto> searchPublicEvents(PublicEventFilter filter) {
        List<EventShortDto> dtos = eventMapper.toEventsShortDto(eventRepository.searchEventsByPublic(filter));

        if (dtos != null && !dtos.isEmpty()) {
            var uris = dtos.stream().map(d -> "/events/" + d.getId()).collect(Collectors.toList());
            var hits = fetchHitsForUris(uris);
            for (EventShortDto dto : dtos) {
                dto.setViews(hits.getOrDefault("/events/" + dto.getId(), 0L));
            }
        }

        if (filter.getSort() != null && filter.getSort() == EventSort.VIEWS) {
            dtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        saveHit();

        return dtos;
    }

    public EventFullDto findPublicEventById(long id) {
        Event event = findByPublicId(id);
        saveHit();
        EventFullDto dto = eventMapper.toEventFullDto(event);
        if (dto != null) {
            var uri = "/events/" + dto.getId();
            var hits = fetchHitsForUris(List.of(uri));
            dto.setViews(hits.getOrDefault(uri, 0L));
        }
        return dto;
    }

    public List<EventFullDto> searchEventsByAdmin(AdminEventFilter filter) {
        List<EventFullDto> dtos = eventMapper.toEventsFullDto(eventRepository.searchEventsByAdmin(filter));
        if (dtos != null && !dtos.isEmpty()) {
            var uris = dtos.stream().map(d -> "/events/" + d.getId()).collect(Collectors.toList());
            var hits = fetchHitsForUris(uris);
            for (EventFullDto dto : dtos) {
                dto.setViews(hits.getOrDefault("/events/" + dto.getId(), 0L));
            }
        }
        return dtos;
    }

    @Transactional
    public EventFullDto moderateEvent(Long eventId, UpdateEventAdminRequest adminRequest) {
        Event event = findById(eventRepository, eventId, "Event");

        if (adminRequest.getEventDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (adminRequest.getEventDate().isBefore(now.plusHours(1))) {
                throw new ExistException("Event date must be at least one hour in the future to publish.");
            }
        }

        eventMapper.updateEventFromAdminDto(adminRequest, event);

        if (adminRequest.getStateAction() != null) {
            if (event.getState().equals(EventState.PENDING)) {
                if (adminRequest.getStateAction().equals(AdminStateAction.PUBLISH_EVENT))
                    event.setState(EventState.PUBLISHED);
                if (adminRequest.getStateAction().equals(AdminStateAction.REJECT_EVENT))
                    event.setState(EventState.CANCELED);
            } else {
                throw new ExistException("Cannot publish the event because it's not in the right state: PUBLISHED");
            }
        }

        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    private void saveHit() {
        try {
            EndpointHitDto hitDto = EndpointHitDto.builder()
                    .app("ewm-main-service")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();
            statsClient.saveHit(hitDto);
        } catch (Exception e) {
            log.error("Не удалось отправить информацию о просмотре в сервис статистики: {}", e.getMessage());
        }
    }

    private Map<String, Long> fetchHitsForUris(List<String> uris) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(10);
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            var stats = statsClient.getStats(start, end, uris, false);
            if (stats == null || stats.isEmpty()) return Map.of();
            return stats.stream().collect(Collectors.toMap(
                    ViewStatsDto::getUri, v -> v.getHits() == null ? 0L : v.getHits()));
        } catch (Exception e) {
            log.error("Не удалось получить просмотры из сервиса статистики: {}", e.getMessage());
            return Map.of();
        }
    }
}
