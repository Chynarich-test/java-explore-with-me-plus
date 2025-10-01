package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    public StatsServiceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            log.warn("Запрошена статистика для неправильного временного диапазона: start={}, end={}", start, end);
            throw new ValidationException("Запрошена статистика для неправильного временного диапазона");
        }

        log.info("Запрос статистики: start={}, end={}, uris={}, unique={}", start, end, uris.size(), unique);

        return statsRepository.getStats(start, end, uris, unique);
    }
}
