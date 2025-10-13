package ru.yandex.practicum.location.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.location.model.Location;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByLatAndLon(float lat, float lon);
}
