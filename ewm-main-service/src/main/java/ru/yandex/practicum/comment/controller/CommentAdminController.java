package ru.yandex.practicum.comment.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.service.CommentService;
import ru.yandex.practicum.user.dto.PageParams;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class CommentAdminController {

    private final CommentService commentService;

    // Получить список комментариев (все, по пользователю или событию, с флагом includeDeleted)
    @GetMapping
    public List<CommentDto> getAllComments(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        PageParams params = new PageParams(from, size);
        return commentService.getAllComments(eventId, authorId, includeDeleted, params);
    }

    //Получить конкретный комментарий (включая удалённые)
    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable Long commentId) {
        return commentService.getCommentById(commentId);
    }

    // Редактировать комментарий (например, для модерации)
    @PatchMapping("/{commentId}")
    public CommentDto updateCommentText(
            @PathVariable Long commentId,
            @RequestBody @NotBlank String newText
    ) {
        return commentService.adminUpdateComment(commentId, newText);
    }

    // Удалить комментарий (soft)
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        commentService.adminDeleteComment(commentId);
    }

    //Восстановить ранее удалённый комментарий
    @PatchMapping("/{commentId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreComment(@PathVariable Long commentId) {
        commentService.restoreComment(commentId);
    }
}