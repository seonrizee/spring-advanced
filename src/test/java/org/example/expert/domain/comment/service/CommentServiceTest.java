package org.example.expert.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "test@test.com";
    private static final Long TODO_ID = 1L;
    private static final String COMMENT_CONTENTS = "Test Contents";

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    private AuthUser authUser;
    private User user;
    private Todo todo;
    private Comment comment;
    private CommentSaveRequest commentSaveRequest;

    @BeforeEach
    void setUp() {
        authUser = new AuthUser(USER_ID, USER_EMAIL, UserRole.USER);
        user = User.fromAuthUser(authUser);
        todo = new Todo("title", "contents", "weather", user);
        comment = new Comment(COMMENT_CONTENTS, user, todo);
        commentSaveRequest = new CommentSaveRequest(COMMENT_CONTENTS);
    }

    @Test
    @DisplayName("댓글 등록 성공")
    public void saveComment_success() {
        // given
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, TODO_ID, commentSaveRequest);

        // then
        assertNotNull(result);
        assertThat(result.getContents()).isEqualTo(COMMENT_CONTENTS);
        assertThat(result.getUser().getId()).isEqualTo(USER_ID);
        verify(todoRepository, times(1)).findById(TODO_ID);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 등록 시 할일을 찾지 못하면 예외 발생")
    public void saveComment_todoNotFound_throwsException() {
        // given
        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, TODO_ID, commentSaveRequest);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
        verify(todoRepository, times(1)).findById(TODO_ID);
        verify(commentRepository, times(0)).save(any());
    }


    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getComments_success() {
        // given
        given(commentRepository.findByTodoIdWithUser(TODO_ID)).willReturn(Collections.singletonList(comment));

        // when
        List<CommentResponse> responses = commentService.getComments(TODO_ID);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getContents()).isEqualTo(COMMENT_CONTENTS);
        assertThat(responses.get(0).getUser().getId()).isEqualTo(USER_ID);
        verify(commentRepository, times(1)).findByTodoIdWithUser(TODO_ID);
    }

    @Test
    @DisplayName("댓글이 없을 때 빈 목록 조회")
    void getComments_emptyList() {
        // given
        given(commentRepository.findByTodoIdWithUser(TODO_ID)).willReturn(Collections.emptyList());

        // when
        List<CommentResponse> responses = commentService.getComments(TODO_ID);

        // then
        assertThat(responses).isEmpty();
        verify(commentRepository, times(1)).findByTodoIdWithUser(TODO_ID);
    }
}
