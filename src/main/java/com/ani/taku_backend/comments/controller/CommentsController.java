package com.ani.taku_backend.comments.controller;

import com.ani.taku_backend.comments.model.dto.CommentsCreateRequestDTO;
import com.ani.taku_backend.comments.model.dto.CommentsUpdateRequestDTO;
import com.ani.taku_backend.comments.service.CommentsService;
import com.ani.taku_backend.common.response.CommonResponse;
import jakarta.persistence.Column;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/comments")
@Slf4j
public class CommentsController {

    private final CommentsService commentsService;

    @PostMapping
    public CommonResponse<Long> createComments(@Valid CommentsCreateRequestDTO commentsCreateRequestDTO) {

        Long saveCommentsId = commentsService.createComments(commentsCreateRequestDTO, null);
        return CommonResponse.created(saveCommentsId);
    }

    @PutMapping("/{commentsId}")
    public CommonResponse<Long> updateComments(@PathVariable("commentsId") Long commentsId,
                    @Valid CommentsUpdateRequestDTO commentsUpdateRequestDTO) {

        Long updateCommentsId = commentsService.updateComments(commentsId, commentsUpdateRequestDTO, null);
        return CommonResponse.ok(updateCommentsId);
    }

    @DeleteMapping("/{commentsId}")
    public CommonResponse<Void> deleteComments(@PathVariable("commentsId") long commentsId) {

        commentsService.deleteComments( commentsId, null);
        return CommonResponse.ok(null);
    }
}
