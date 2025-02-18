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
import jakarta.validation.Valid;
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

    @Operation(summary = "채팅방 목록 가져오기")
    @GetMapping
    public CommonResponse<List<ChatRoomResponseDTO>> getChatRoomList(
            @AuthenticationPrincipal PrincipalUser principalUser) {
        long startTime = System.nanoTime();
        List<ChatRoomResponseDTO> chatRooms = chatRoomService.findChatRoomList(principalUser.getUserId());
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;
        System.out.println("Execution Time: " + duration + " ms");
        return CommonResponse.ok(chatRooms);
    }

    @Operation(summary = "특정 채팅방 조회")
    @GetMapping("/{wsRoomId}")
    public CommonResponse<ChatRoomResponseDTO> getChatRoom(
            @PathVariable String wsRoomId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        ChatRoomResponseDTO chatRoom = chatRoomService.findChatRoom(wsRoomId, principalUser.getUserId());
        return CommonResponse.ok(chatRoom);
    }

    @Operation(summary = "채팅방 별 안 읽은 메세지 갯수 반환")
    @GetMapping("/{wsRoomId}/unread")
    public CommonResponse<Integer> getChatRoomUnreadCount(
            @PathVariable String wsRoomId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        Integer unreadCount = chatRoomService.getChatRoomUnreadCount(wsRoomId, principalUser.getUserId());
        return CommonResponse.ok(unreadCount);
    }

    @Operation(summary = "총 안 읽음 메세지 갯수 반환")
    @GetMapping("/unread/total")
    public CommonResponse<Integer> getTotalUnreadCount(
            @AuthenticationPrincipal PrincipalUser principalUser) {
        Integer totalUnreadCount = chatRoomService.getTotalUnreadCount(principalUser.getUserId());
        return CommonResponse.ok(totalUnreadCount);
    }

    @Operation(summary = "채팅 메세지 전송")
    @PostMapping("/send")
    public CommonResponse<Void> sendMessage(
            @RequestParam String wsRoomId,
            @AuthenticationPrincipal PrincipalUser principalUser,
            @RequestParam String content) {
        chatService.sendMessageByWsRoomId(wsRoomId, principalUser.getUserId(), content);
        return CommonResponse.ok(null);
    }

    @Operation(summary = "채팅방 나가기")
    @PostMapping("/leave")
    public CommonResponse<Void> leaveRoom(
            @RequestParam String wsRoomId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        chatService.leaveRoomByWsRoomId(wsRoomId, principalUser.getUserId());
        return CommonResponse.ok(null);
    }

    @Operation(summary = "읽은 메세지 처리", description = "마지막으로 읽은 메세지 id 반환")
    @PostMapping("/mark-as-read")
    public CommonResponse<Void> markMessagesAsRead(
            @RequestParam String wsRoomId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        chatService.markMessagesAsReadByWsRoomId(wsRoomId, principalUser.getUserId());
        return CommonResponse.ok(null);
    }
}