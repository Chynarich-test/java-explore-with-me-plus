package ru.yandex.practicum.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    default List<Comment> findAllByEventId(Long eventId, Pageable pageable) {
        return findAll(
                CommentSpecs.fetchAll()
                        .and(CommentSpecs.byEventId(eventId))
                        .and(CommentSpecs.notDeleted()),
                pageable
        ).getContent();
    }

    default List<Comment> findAllFiltered(Long eventId, Long authorId, Boolean includeDeleted, Pageable pageable) {
        Specification<Comment> spec = CommentSpecs.fetchAll();

        if (eventId != null) {
            spec = spec.and(CommentSpecs.byEventId(eventId));
        }
        if (authorId != null) {
            spec = spec.and(CommentSpecs.byAuthorId(authorId));
        }
        if (includeDeleted == null || !includeDeleted) {
            spec = spec.and(CommentSpecs.notDeleted());
        }

        return findAll(spec, pageable).getContent();
    }

    // Опционально, так как не знаю как поведут себя тесты постмана

    // Кол-во комментариев по списку событий
    @Query("""
        SELECT c.event.id, COUNT(c)
        FROM Comment c
        WHERE c.event.id IN :eventIds
        GROUP BY c.event.id
    """)
    List<Object[]> countCommentsForEvents(List<Long> eventIds);

    // Последние комментарии (например, 3 штуки на событие)
    @Query("""
        SELECT c
        FROM Comment c
        WHERE c.event.id IN :eventIds
        ORDER BY c.createdOn DESC
    """)
    List<Comment> findRecentCommentsForEvents(List<Long> eventIds);
}