package ru.yandex.practicum.repository;

import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StatsRepository {
    EndpointHit save(EndpointHit hit);

    Optional<EndpointHit> findById(Long id);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    List<ViewStatsDto> getUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);
}