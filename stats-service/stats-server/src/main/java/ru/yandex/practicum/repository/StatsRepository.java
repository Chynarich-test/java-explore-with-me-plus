package ru.yandex.practicum.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.ViewStatsDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StatsRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public StatsRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        StringBuilder sqlBuilder = new StringBuilder();
        if (unique) {
            sqlBuilder.append("select app, uri, count(distinct ip) as count");
        } else {
            sqlBuilder.append("select app, uri, count(ip) as count");
        }

        sqlBuilder.append(" from hits where timestamp between :start and :end");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start", start)
                .addValue("end", end);

        if (uris != null && !uris.isEmpty()) {
            sqlBuilder.append(" and uri in (:uris)");
            params.addValue("uris", uris);
        }

        sqlBuilder.append(" group by app, uri order by count desc");


        return namedParameterJdbcTemplate.query(sqlBuilder.toString(), params, this::mapRowToViewStatsDto);
    }

    private ViewStatsDto mapRowToViewStatsDto(ResultSet resultSet, int rowNum) throws SQLException {
        return ViewStatsDto.builder()
                .app(resultSet.getString("app"))
                .uri(resultSet.getString("uri"))
                .hits(resultSet.getLong("count"))
                .build();
    }

}
