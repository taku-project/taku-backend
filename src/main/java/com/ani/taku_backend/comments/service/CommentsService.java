package com.ani.taku_backend.comments.service;

import com.ani.taku_backend.comments.model.dto.CommentsCreateRequestDTO;
import com.ani.taku_backend.comments.model.entity.Comments;
import com.ani.taku_backend.comments.repository.CommentsRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.annotation.ValidateProfanity;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.post.service.PostService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ani.taku_backend.common.exception.ErrorCode.NOT_FOUND_COMMENTS;
import static com.ani.taku_backend.common.exception.ErrorCode.NOT_FOUND_POST;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final PostRepository postRepository;
    private final BlackUserService blackUserService;

    @Transactional
    @RequireUser
    @ValidateProfanity(fields = {"content"})
    public Long createComments(CommentsCreateRequestDTO commentsCreateRequestDTO, PrincipalUser principalUser) {

        // Black 유저 검증
        User user = blackUserService.validateBlackUser(principalUser);

        if (commentsCreateRequestDTO.getPostId() == null) {
            throw new DuckwhoException(NOT_FOUND_POST);
        }

        // DB에 클라이언트에서 전송된 PostId의 게시글이 없으면 예외
        Post post = postRepository.findById(commentsCreateRequestDTO.getPostId())
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));
        log.info("Post 조회 성공: {}", post.getId());

        Long saveCommentsId = saveComments(commentsCreateRequestDTO, user, post);
        log.info("commentsId: {}", saveCommentsId);

        return saveCommentsId;
    }

    // 넘어오는 parentCommentsId가 null이면 댓글, 값이 있으면 해당 댓글의 댓글
    private Long saveComments(CommentsCreateRequestDTO commentsCreateRequestDTO, User user, Post post) {
        Comments savedComments = null;

        if (commentsCreateRequestDTO.getParentCommentId() == null) {
            Comments comments = Comments.createComments(user, post, commentsCreateRequestDTO.getContent());
            savedComments = commentsRepository.save(comments);
            log.info("댓글 저장 완료, savedComments: {}", savedComments);

        } else {
            long parentCommentsId = commentsCreateRequestDTO.getParentCommentId();
            Comments parentComments = commentsRepository.findById(parentCommentsId)
                    .orElseThrow(() -> new DuckwhoException(NOT_FOUND_COMMENTS));
            Comments comments = Comments.createCommentsReply(user, post, parentComments, commentsCreateRequestDTO.getContent());
            savedComments = commentsRepository.save(comments);
            log.info("대댓글 저장 완료, savedComments: {}", savedComments);

        }
        return savedComments.getId();
    }
}
