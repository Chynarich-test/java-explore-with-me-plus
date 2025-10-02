package ru.yandex.practicum.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.model.EndpointHit;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StatsRepositoryImpl implements StatsRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<EndpointHit> HIT_ROW_MAPPER = (rs, rowNum) ->
            EndpointHit.builder()
                    .id(rs.getLong("id"))
                    .app(rs.getString("app"))
                    .uri(rs.getString("uri"))
                    .ip(rs.getString("ip"))
                    .timestamp(rs.getTimestamp("timestamp").toLocalDateTime())
                    .build();

    private static final RowMapper<ViewStatsDto> STATS_ROW_MAPPER = (rs, rowNum) ->
            ViewStatsDto.builder()
                    .app(rs.getString("app"))
                    .uri(rs.getString("uri"))
                    .hits(rs.getLong("hits"))
                    .build();

    @Override
    public EndpointHit save(EndpointHit hit) {
        String sql = "INSERT INTO hits (app, uri, ip, timestamp) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, hit.getApp());
            ps.setString(2, hit.getUri());
            ps.setString(3, hit.getIp());
            ps.setTimestamp(4, Timestamp.valueOf(hit.getTimestamp()));
            return ps;
        }, keyHolder);

        hit.setId(keyHolder.getKey().longValue());
        return hit;
    }

    @Override
    public Optional<EndpointHit> findById(Long id) {
        String sql = "SELECT * FROM hits WHERE id = ?";
        try {
            EndpointHit hit = jdbcTemplate.queryForObject(sql, HIT_ROW_MAPPER, id);
            return Optional.ofNullable(hit);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        String sql;
        Object[] params;

        if (uris != null && !uris.isEmpty()) {
            sql = "SELECT app, uri, COUNT(ip) as hits " +
                    "FROM hits " +
                    "WHERE timestamp BETWEEN ? AND ? " +
                    "AND uri IN (" + buildPlaceholders(uris.size()) + ") " +
                    "GROUP BY app, uri " +
                    "ORDER BY hits DESC";
            params = buildParams(start, end, uris);
        } else {
            sql = "SELECT app, uri, COUNT(ip) as hits " +
                    "FROM hits " +
                    "WHERE timestamp BETWEEN ? AND ? " +
                    "GROUP BY app, uri " +
                    "ORDER BY hits DESC";
            params = new Object[]{Timestamp.valueOf(start), Timestamp.valueOf(end)};
        }

        return jdbcTemplate.query(sql, STATS_ROW_MAPPER, params);
    }

    @Override
    public List<ViewStatsDto> getUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        String sql;
        Object[] params;

        if (uris != null && !uris.isEmpty()) {
            sql = "SELECT app, uri, COUNT(DISTINCT ip) as hits " +
                    "FROM hits " +
                    "WHERE timestamp BETWEEN ? AND ? " +
                    "AND uri IN (" + buildPlaceholders(uris.size()) + ") " +
                    "GROUP BY app, uri " +
                    "ORDER BY hits DESC";
            params = buildParams(start, end, uris);
        } else {
            sql = "SELECT app, uri, COUNT(DISTINCT ip) as hits " +
                    "FROM hits " +
                    "WHERE timestamp BETWEEN ? AND ? " +
                    "GROUP BY app, uri " +
                    "ORDER BY hits DESC";
            params = new Object[]{Timestamp.valueOf(start), Timestamp.valueOf(end)};
        }

        return jdbcTemplate.query(sql, STATS_ROW_MAPPER, params);
    }

    private String buildPlaceholders(int count) {
        return String.join(",", java.util.Collections.nCopies(count, "?"));
    }

    private Object[] buildParams(LocalDateTime start, LocalDateTime end, List<String> uris) {
        Object[] params = new Object[2 + uris.size()];
        params[0] = Timestamp.valueOf(start);
        params[1] = Timestamp.valueOf(end);
        for (int i = 0; i < uris.size(); i++) {
            params[2 + i] = uris.get(i);
        }
        return params;
    }
}