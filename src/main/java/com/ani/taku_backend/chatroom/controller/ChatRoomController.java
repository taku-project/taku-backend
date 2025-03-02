package com.ani.taku_backend.chatroom.controller;

import com.ani.taku_backend.chatroom.model.dto.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.service.ChatRoomService;
import com.ani.taku_backend.chatroom.service.ChatService;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.ani.taku_backend.user.model.dto.PrincipalUser;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    /**
     * 채팅방을 생성합니다.
     *
     * @param articleId 생성할 채팅방에 해당하는 상품 ID
     * @param principalUser 현재 인증된 사용자
     * @return 생성된 채팅방 정보
     */
    @Operation(
            summary = "채팅방 생성",
            description = "새로운 채팅방을 생성합니다. 판매자는 상품 정보에서 자동으로 설정됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "채팅방 생성 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 채팅방",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            )
    })
    @PostMapping
    public CommonResponse<ChatRoomResponseDTO> createChatRoom(
            @Parameter(description = "상품 ID", required = true, example = "1")
            @RequestParam Long articleId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        ChatRoomRequestDTO requestDto = new ChatRoomRequestDTO(articleId, principalUser.getUserId());
        ChatRoomResponseDTO responseDto = chatRoomService.createChatRoom(requestDto);
        return CommonResponse.created(responseDto);
    }

    /**
     * 현재 사용자의 채팅방 목록을 조회합니다.
     *
     * @param principalUser 현재 인증된 사용자
     * @return 사용자의 채팅방 목록
     */
    @Operation(summary = "채팅방 목록 가져오기")
    @GetMapping
    public CommonResponse<List<ChatRoomResponseDTO>> getChatRoomList(
            @AuthenticationPrincipal PrincipalUser principalUser) {
        List<ChatRoomResponseDTO> chatRooms = chatRoomService.findChatRoomList(principalUser.getUserId());
        return CommonResponse.ok(chatRooms);
    }

    /**
     * 특정 채팅방의 상세 정보를 조회합니다.
     *
     * @param wsRoomId 채팅방의 WebSocket ID
     * @param principalUser 현재 인증된 사용자
     * @return 해당 채팅방의 상세 정보
     */
    @Operation(summary = "특정 채팅방 조회")
    @GetMapping("/{wsRoomId}")
    public CommonResponse<ChatRoomResponseDTO> getChatRoom(
            @PathVariable String wsRoomId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        ChatRoomResponseDTO chatRoom = chatRoomService.findChatRoom(wsRoomId, principalUser.getUserId());
        return CommonResponse.ok(chatRoom);
    }

    /**
     * 특정 채팅방의 안 읽은 메세지 개수를 반환합니다.
     *
     * @param wsRoomId 채팅방의 WebSocket ID
     * @param principalUser 현재 인증된 사용자
     * @return 안 읽은 메세지 개수
     */
    @Operation(summary = "채팅방 별 안 읽은 메세지 갯수 반환")
    @GetMapping("/{wsRoomId}/unread")
    public CommonResponse<Integer> getChatRoomUnreadCount(
            @PathVariable String wsRoomId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        Integer unreadCount = chatRoomService.getChatRoomUnreadCount(wsRoomId, principalUser.getUserId());
        return CommonResponse.ok(unreadCount);
    }

    /**
     * 모든 채팅방의 총 안 읽은 메세지 개수를 반환합니다.
     *
     * @param principalUser 현재 인증된 사용자
     * @return 총 안 읽은 메세지 개수
     */
    @Operation(summary = "총 안 읽음 메세지 갯수 반환")
    @GetMapping("/unread/total")
    public CommonResponse<Integer> getTotalUnreadCount(
            @AuthenticationPrincipal PrincipalUser principalUser) {
        Integer totalUnreadCount = chatRoomService.getTotalUnreadCount(principalUser.getUserId());
        return CommonResponse.ok(totalUnreadCount);
    }

/*    *//**
     * 지정된 채팅방에 메세지를 전송합니다.
     *
     * @param wsRoomId 채팅방의 WebSocket ID
     * @param principalUser 현재 인증된 사용자
     * @param content 전송할 메세지 내용
     * @return 결과가 없는 응답
     *//*
    @Operation(summary = "채팅 메세지 전송")
    @PostMapping("/send")
    public CommonResponse<Void> sendMessage(
            @RequestParam String wsRoomId,
            @AuthenticationPrincipal PrincipalUser principalUser,
            @RequestParam String content) {
        chatService.sendMessageByWsRoomId(wsRoomId, principalUser.getUserId(), content);
        return CommonResponse.ok(null);
    }*/

    /**
     * 사용자가 채팅방을 나가도록 처리합니다.
     *
     * @param wsRoomId 채팅방의 WebSocket ID
     * @param principalUser 현재 인증된 사용자
     * @return 결과가 없는 응답
     */
    @Operation(summary = "채팅방 나가기")
    @PostMapping("/leave")
    public CommonResponse<Void> leaveRoom(
            @RequestParam String wsRoomId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        chatService.leaveRoomByWsRoomId(wsRoomId, principalUser.getUserId());
        return CommonResponse.ok(null);
    }

    /**
     * 지정된 채팅방의 메세지들을 읽은 상태로 변경합니다.
     *
     * @param wsRoomId 채팅방의 WebSocket ID
     * @param principalUser 현재 인증된 사용자
     * @return 결과가 없는 응답
     */
    @Operation(summary = "읽은 메세지 처리", description = "마지막으로 읽은 메세지 id 반환")
    @PostMapping("/mark-as-read")
    public CommonResponse<Void> markMessagesAsRead(
            @RequestParam String wsRoomId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        chatService.markMessagesAsReadByWsRoomId(wsRoomId, principalUser.getUserId());
        return CommonResponse.ok(null);
    }
}