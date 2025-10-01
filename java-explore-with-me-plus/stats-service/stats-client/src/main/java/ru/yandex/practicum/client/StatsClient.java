package ru.yandex.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpStatusCodeException;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class StatsClient {

    private final RestClient restClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-service.url}") String serverUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(serverUrl)
                .build();
    }

    public void saveHit(EndpointHitDto hitDto) {
        try {
            restClient.post()
                    .uri("/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(hitDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String errorBody = res.getBodyAsString(StandardCharsets.UTF_8);
                        throw new StatsClientException("Ошибка клиента (4xx) при обращении к StatsService: "
                                + (errorBody.isBlank() ? "сообшение ошибки не предоставлено" : errorBody));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        String errorBody = res.getBodyAsString(StandardCharsets.UTF_8);
                        throw new StatsClientException("Ошибка сервера (5xx) в StatsService: "
                                + (errorBody.isBlank() ? "сообшение ошибки не предоставлено" : errorBody));
                    })
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Не удалось отправить хит в StatsService", e);
            throw new StatsClientException("Не удалось отправить хит в StatsService", e);
        }
    }

    public List<ViewStats> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/stats")
                                .queryParam("start", start.format(FORMATTER))
                                .queryParam("end", end.format(FORMATTER))
                                .queryParam("unique", unique);
                        if (uris != null && !uris.isEmpty()) {
                            for (String u : uris) {
                                uriBuilder.queryParam("uris", u);
                            }
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String errorBody = res.getBodyAsString(StandardCharsets.UTF_8);
                        throw new StatsClientException("Ошибка клиента (4xx) при обращении к StatsService: "
                                + (errorBody.isBlank() ? "сообшение ошибки не предоставлено" : errorBody));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        String errorBody = res.getBodyAsString(StandardCharsets.UTF_8);
                        throw new StatsClientException("Ошибка сервера (5xx) в StatsService: "
                                + (errorBody.isBlank() ? "сообшение ошибки не предоставлено" : errorBody));
                    })
                    .body(new org.springframework.core.ParameterizedTypeReference<List<ViewStats>>() {});
        } catch (RestClientException e) {
            log.error("Не удалось получить статистику из StatsService", e);
            throw new StatsClientException("Не удалось получить статистику из StatsService", e);
        }
    }
}