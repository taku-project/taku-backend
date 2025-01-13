package com.ani.taku_backend.comments.service;

import com.ani.taku_backend.comments.model.dto.CommentsCreateRequestDTO;
import com.ani.taku_backend.comments.model.dto.CommentsUpdateRequestDTO;
import com.ani.taku_backend.comments.model.entity.Comments;
import com.ani.taku_backend.comments.repository.CommentsRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.annotation.ValidateProfanity;
import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ani.taku_backend.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final PostRepository postRepository;
    private final BlackUserService blackUserService;

    /**
     * 댓글 / 대댓글 생성
     */
    @Transactional
    @RequireUser
    @ValidateProfanity(fields = {"content"})
    public Long createComments(CommentsCreateRequestDTO commentsCreateRequestDTO, PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);       // Black 유저 검증

        // 넘어온 postId가 없으면 예외
        if (commentsCreateRequestDTO.getPostId() == null) {
            throw new DuckwhoException(NOT_FOUND_COMMENTS);
        }

        // DB에 클라이언트에서 전송된 PostId의 게시글이 없으면 예외
        Post post = postRepository.findById(commentsCreateRequestDTO.getPostId())
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_COMMENTS));
        log.info("Post 조회 성공: {}", post.getId());

        // 댓글 저장 메서드
        return saveComments(commentsCreateRequestDTO, user, post);
    }

    /**
     * 댓글 / 대댓글 수정
     */
    @Transactional
    @RequireUser
    @ValidateProfanity(fields = {"content"})
    public Long updateComments(long commentsId, @Valid CommentsUpdateRequestDTO commentsUpdateRequestDTO, PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);         // Black 유저 검증

        // 넘어온 PostId가 없으면 예외
        if (commentsUpdateRequestDTO.getPostId() == null) {
            throw new DuckwhoException(NOT_FOUND_COMMENTS);
        }

        return updateComments(commentsId, commentsUpdateRequestDTO, user);         // 댓글 업데이트

    }

    /**
     * 댓글 / 대댓글 삭제
     */
    @Transactional
    @RequireUser
    public void deleteComments(@Valid long commentId, PrincipalUser principalUser) {
        User user = blackUserService.checkBlackUser(principalUser);         // Black 유저 검증
        deleteComments(commentId, user);                                    // 댓글 삭제
    }

    // 넘어오는 parentCommentsId가 null이면 댓글, 값이 있으면 해당 댓글의 댓글을 저장
    private long saveComments(CommentsCreateRequestDTO commentsCreateRequestDTO, User user, Post post) {
        Comments savedComments = null;

        // 부모아이디 null == 댓글
        if (commentsCreateRequestDTO.getParentCommentId() == null) {
            Comments comments = Comments.createComments(user, post, commentsCreateRequestDTO.getContent());

            savedComments = commentsRepository.save(comments);
            log.debug("댓글 저장 완료, savedComments: {}", savedComments);

        // 부모아이디 null != 넘어온 댓글 Id의 댓글, 즉 대댓글
        } else {
            long parentCommentsId = commentsCreateRequestDTO.getParentCommentId();

            // 부모 댓글을 조회
            Comments parentComments = commentsRepository.findById(parentCommentsId)
                    .orElseThrow(() -> new DuckwhoException(NOT_FOUND_COMMENTS));

            Comments comments = Comments.createCommentsReply(user, post, parentComments, commentsCreateRequestDTO.getContent());
            savedComments = commentsRepository.save(comments);
            log.debug("대댓글 저장 완료, savedComments: {}", savedComments);
        }
        return savedComments.getId();
    }

    // 댓글 업데이트 로직
    private long updateComments(Long commentsId, CommentsUpdateRequestDTO commentsUpdateRequestDTO, User user) {
        Comments findComments = commentsRepository.findById(commentsId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_COMMENTS));

        checkDeleteComments(findComments);                   // 삭제 여부 검증
        checkAuthorAndAdmin(user, findComments);             // 작성자, 관리자 검증

        // 본문 수정
        findComments.updateComments(commentsUpdateRequestDTO.getContent());
        log.debug("댓글 본문 업데이트 완료 {}", commentsId);

        return findComments.getId();
    }

    // 댓글 삭제 로직
    private void deleteComments(long commentId, User user) {
        Comments findComments = commentsRepository.findById(commentId).orElseThrow(() -> new DuckwhoException(NOT_FOUND_COMMENTS));
        checkAuthorAndAdmin(user, findComments);        // 작성자, 관리자 검증
        checkDeleteComments(findComments);              // 삭제 검증
        findComments.delete();
        log.debug("댓글 삭제 완료, commentsDeleteAt: {}", findComments.getDeletedAt());
    }

    // 해당 댓글이 삭제 처리 되어있으면 예외
    private void checkDeleteComments(Comments findComments) {
        if (findComments.getDeletedAt() != null) {
            throw new DuckwhoException(NOT_FOUND_COMMENTS);
        }
    }

    // 어드민이거나, 작성자와 다르면 예외
    private void checkAuthorAndAdmin(User user, Comments findComments) {
        if ((!user.getRole().equals(UserRole.ADMIN.name())) &&
                !user.getUserId().equals(findComments.getUser().getUserId())) {
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }
    }
}
