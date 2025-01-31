package com.ani.taku_backend.chatroom.controller;

import com.ani.taku_backend.chatroom.model.dto.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.service.ChatRoomService;
import com.ani.taku_backend.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat Room API", description = "채팅방 관련 API")
@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "채팅방 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 채팅방")
    })
    @PostMapping
    public CommonResponse<ChatRoomResponseDTO> createChatRoom(
            @Parameter(description = "채팅방 생성 정보", required = true)
            @Valid @RequestBody ChatRoomRequestDTO requestDto) {
        ChatRoomResponseDTO responseDto = chatRoomService.createChatRoom(requestDto);
        return CommonResponse.created(responseDto);
    }

    @Operation(summary = "채팅방 목록 조회", description = "사용자의 모든 채팅방 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping
    public CommonResponse<List<ChatRoomResponseDTO>> getChatRoomList(
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId) {
        List<ChatRoomResponseDTO> chatRooms = chatRoomService.findChatRoomList(userId);
        return CommonResponse.ok(chatRooms);
    }

    @Operation(summary = "특정 채팅방 조회", description = "채팅방 ID로 특정 채팅방을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @GetMapping("/{roomId}")
    public CommonResponse<ChatRoomResponseDTO> getChatRoom(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable String roomId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId) {
        ChatRoomResponseDTO chatRoom = chatRoomService.findChatRoom(roomId, userId);
        return CommonResponse.ok(chatRoom);
    }

    @Operation(summary = "채팅방 안 읽은 메시지 수 조회", description = "특정 채팅방의 안 읽은 메시지 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "안 읽은 메시지 수 조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @GetMapping("/{roomId}/unread")
    public CommonResponse<Integer> getChatRoomUnreadCount(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable String roomId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId) {
        Integer unreadCount = chatRoomService.getChatRoomUnreadCount(roomId, userId);
        return CommonResponse.ok(unreadCount);
    }

    @Operation(summary = "전체 안 읽은 메시지 수 조회", description = "사용자의 모든 채팅방의 안 읽은 메시지 총 개수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 안 읽은 메시지 수 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/unread/total")
    public CommonResponse<Integer> getTotalUnreadCount(
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId) {
        Integer totalUnreadCount = chatRoomService.getTotalUnreadCount(userId);
        return CommonResponse.ok(totalUnreadCount);
    }
}