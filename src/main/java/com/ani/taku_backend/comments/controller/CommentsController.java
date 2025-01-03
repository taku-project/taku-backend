package com.ani.taku_backend.comments.controller;

import com.ani.taku_backend.comments.model.dto.CommentsCreateRequestDTO;
import com.ani.taku_backend.comments.service.CommentsService;
import com.ani.taku_backend.common.response.CommonResponse;
import jakarta.persistence.Column;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
