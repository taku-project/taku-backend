package com.ani.taku_backend.comments.service;

import com.ani.taku_backend.comments.model.dto.CommentsCreateRequestDTO;
import com.ani.taku_backend.comments.model.dto.CommentsResponseDTO;
import com.ani.taku_backend.comments.model.dto.CommentsUpdateRequestDTO;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.validation.Valid;
import java.util.List;

public interface CommentsService {
    Long createComments(CommentsCreateRequestDTO commentsCreateRequestDTO, User user);

    Long updateComments(long commentsId, @Valid CommentsUpdateRequestDTO commentsUpdateRequestDTO, User user);

    void deleteComments(long commentId, User user);

    List<CommentsResponseDTO> getPostComments(Long postId, Long currentUserId);

    long getCommentCount(Long postId);
}
