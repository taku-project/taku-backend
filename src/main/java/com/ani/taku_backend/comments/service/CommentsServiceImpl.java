package com.ani.taku_backend.comments.service;

import com.ani.taku_backend.comments.model.dto.CommentsCreateRequestDTO;
import com.ani.taku_backend.comments.model.dto.CommentsResponseDTO;
import com.ani.taku_backend.comments.model.dto.CommentsUpdateRequestDTO;
import com.ani.taku_backend.comments.model.entity.Comments;
import com.ani.taku_backend.comments.repository.CommentsRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.annotation.ValidateProfanity;
import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ani.taku_backend.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentsServiceImpl implements CommentsService {

    private final CommentsRepository commentsRepository;
    private final PostRepository postRepository;

    /**
     * 댓글 / 대댓글 생성
     */
    @Transactional
    @ValidateProfanity(fields = {"content"})
    public Long createComments(CommentsCreateRequestDTO commentsCreateRequestDTO, User user) {

        // 넘어온 postId가 없으면 예외
        if (commentsCreateRequestDTO.getPostId() == null) {
            throw new DuckwhoException(NOT_FOUND_COMMENTS);
        }

        // DB에 클라이언트에서 전송된 PostId의 게시글이 없으면 예외
        Post post = postRepository.findById(commentsCreateRequestDTO.getPostId())
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_COMMENTS));
        log.info("Post 조회 성공: {}", post.getId());

        // 댓글 저장 메서드
        return saveCommentsProcess(commentsCreateRequestDTO, user, post);
    }

    /**
     * 댓글 / 대댓글 수정
     */
    @Transactional
    @RequireUser
    @ValidateProfanity(fields = {"content"})
    public Long updateComments(long commentsId, @Valid CommentsUpdateRequestDTO commentsUpdateRequestDTO, User user) {

        // 넘어온 PostId가 없으면 예외
        if (commentsUpdateRequestDTO.getPostId() == null) {
            throw new DuckwhoException(NOT_FOUND_COMMENTS);
        }

        return updateCommentsProcess(commentsId, commentsUpdateRequestDTO, user);         // 댓글 업데이트
    }

    /**
     * 댓글 / 대댓글 삭제
     */
    @Transactional
    public void deleteComments(long commentId, User user) {

        Comments findComments = commentsRepository.findById(commentId).orElseThrow(() -> new DuckwhoException(NOT_FOUND_COMMENTS));

        checkAuthorAndAdmin(user, findComments);        // 작성자, 관리자 검증
        checkDeleteComments(findComments);              // 삭제 검증

        findComments.delete();
        log.debug("댓글 삭제 완료, commentsDeleteAt: {}", findComments.getDeletedAt());
    }

    // 넘어오는 parentCommentsId가 null이면 댓글, 값이 있으면 해당 댓글의 댓글을 저장
    private long saveCommentsProcess(CommentsCreateRequestDTO commentsCreateRequestDTO, User user, Post post) {
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
    private long updateCommentsProcess(Long commentsId, CommentsUpdateRequestDTO commentsUpdateRequestDTO, User user) {
        Comments findComments = commentsRepository.findById(commentsId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_COMMENTS));

        checkDeleteComments(findComments);                   // 삭제 여부 검증
        checkAuthorAndAdmin(user, findComments);             // 작성자, 관리자 검증

        // 본문 수정
        findComments.updateComments(commentsUpdateRequestDTO.getContent());
        log.debug("댓글 본문 업데이트 완료 {}", commentsId);

        return findComments.getId();
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

    /**
     * 게시글의 댓글 목록 조회
     * - 최상위 댓글과 대댓글을 계층 구조로 조회
     * - 최상위 댓글은 생성일시 기준 내림차순 정렬
     * - 대댓글은 생성일시 기준 오름차순 정렬
     * - 삭제된 댓글은 제외하고 조회
     *
     * @param postId 게시글 ID
     * @param currentUserId 현재 로그인한 사용자 ID (null 가능)
     * @return 댓글 목록 (대댓글 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public List<CommentsResponseDTO> getPostComments(Long postId, Long currentUserId) {
        // 모든 댓글을 한 번에 조회
        List<Comments> allComments = commentsRepository.findAllCommentsWithParent(postId);
        
        // 부모 댓글만 필터링
        return allComments.stream()
                .filter(comment -> comment.getParentComment() == null)  // 부모 댓글만 선택
                .map(parentComment -> {
                    // 현재 부모 댓글의 자식 댓글들 찾기
                    List<CommentsResponseDTO> replyDtos = allComments.stream()
                            .filter(comment -> comment.getParentComment() != null 
                                    && comment.getParentComment().getId() == parentComment.getId())
                            .map(reply -> CommentsResponseDTO.of(reply, currentUserId))
                            .toList();
                    
                    // 부모 댓글 DTO 생성 (대댓글 목록 포함)
                    return CommentsResponseDTO.of(parentComment, currentUserId, replyDtos);
                })
                .toList();
    }
}
