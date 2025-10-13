package ru.yandex.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.request.dto.ConfirmedRequestCount;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.RequestStatus;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request> {

    default List<Request> findAllByEventIdWithRelations(Long eventId) {
        return findAll(RequestSpecs.fetchRequesterAndEvent()
                .and(RequestSpecs.byEvent(eventId)));
    }

    default List<Request> findAllByRequesterId(Long requesterId) {
        return findAll(RequestSpecs.fetchRequesterAndEvent()
                .and(RequestSpecs.byRequester(requesterId)));
    }

    default long countConfirmedRequests(Long eventId) {
        return count(RequestSpecs.byEvent(eventId)
                .and(RequestSpecs.byStatus(RequestStatus.CONFIRMED)));
    }

    default List<Request> findAllByEventIdAndIds(Long eventId, List<Long> ids) {
        return findAll(RequestSpecs.fetchRequesterAndEvent()
                .and(RequestSpecs.byEventAndIds(eventId, ids)));
    }

    @Query("""
        SELECT new ru.yandex.practicum.request.dto.ConfirmedRequestCount(r.event.id, COUNT(r))
        FROM Request r
        WHERE r.status = ru.yandex.practicum.request.model.RequestStatus.CONFIRMED
          AND r.event.id IN :eventIds
        GROUP BY r.event.id
        """)
    List<ConfirmedRequestCount> countConfirmedRequestsForEvents(@Param("eventIds") List<Long> eventIds);
}