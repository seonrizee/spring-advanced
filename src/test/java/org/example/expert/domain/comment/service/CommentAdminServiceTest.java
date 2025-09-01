package org.example.expert.domain.comment.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {

    private static final Long COMMENT_ID = 1L;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentAdminService commentAdminService;

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_success() {
        // given
        Comment comment = new Comment("contents", null, null);
        given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(comment));

        // when
        commentAdminService.deleteComment(COMMENT_ID);

        // then
        verify(commentRepository, times(1)).findById(COMMENT_ID);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 예외 발생")
    void deleteComment_notFound() {
        // given
        given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.empty());

        // when, then
        assertThrows(InvalidRequestException.class, () -> {
            commentAdminService.deleteComment(COMMENT_ID);
        });

        verify(commentRepository, times(1)).findById(COMMENT_ID);
        verify(commentRepository, never()).delete(any());
    }
}
