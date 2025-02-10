package com.ani.taku_backend.chatroom.controller;

import com.ani.taku_backend.chatroom.model.dto.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.service.ChatRoomService;
import com.ani.taku_backend.chatroom.service.ChatService;
import com.ani.taku_backend.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Autowired
    private ChatService chatService;

    @Operation(summary = "채팅방 생성")
    @PostMapping
    public CommonResponse<ChatRoomResponseDTO> createChatRoom(
            @Valid @RequestBody ChatRoomRequestDTO requestDto) {
        ChatRoomResponseDTO responseDto = chatRoomService.createChatRoom(requestDto);
        return CommonResponse.created(responseDto);
    }

    @GetMapping
    public CommonResponse<List<ChatRoomResponseDTO>> getChatRoomList(
            @RequestParam Long userId) {
        List<ChatRoomResponseDTO> chatRooms = chatRoomService.findChatRoomList(userId);
        return CommonResponse.ok(chatRooms);
    }



    @Operation(summary = "특정 채팅방 조회")
    @GetMapping("/{roomId}")
    public CommonResponse<ChatRoomResponseDTO> getChatRoom(
            @PathVariable String roomId,
            @RequestParam Long userId) {
        ChatRoomResponseDTO chatRoom = chatRoomService.findChatRoom(roomId, userId);
        return CommonResponse.ok(chatRoom);
    }

    @GetMapping("/{roomId}/unread")
    public CommonResponse<Integer> getChatRoomUnreadCount(
            @PathVariable String roomId,
            @RequestParam Long userId) {
        Integer unreadCount = chatRoomService.getChatRoomUnreadCount(roomId, userId);
        return CommonResponse.ok(unreadCount);
    }

    @GetMapping("/unread/total")
    public CommonResponse<Integer> getTotalUnreadCount(
            @RequestParam Long userId) {
        Integer totalUnreadCount = chatRoomService.getTotalUnreadCount(userId);
        return CommonResponse.ok(totalUnreadCount);
    }

    @Operation(summary = "채팅 메세지 전송")
    @PostMapping("/send")
    public CommonResponse<Long> sendMessage(@RequestParam Long roomId,
                            @RequestParam Long senderId,
                            @RequestParam String content) {
        chatService.sendMessage(roomId, senderId, content);
        return CommonResponse.ok(null);
    }


    @Operation(summary = "채팅방 나가기")
    // 채팅방 나가기, 개발 완료
    @PostMapping("/leave")
    public CommonResponse<Long>  leaveRoom(@RequestParam Long chatRoomId,
                          @RequestParam Long userId) {
        chatService.leaveRoom(chatRoomId, userId);
        return CommonResponse.ok(null);
    }

    // 메시지 읽음 상태 업데이트
    @PostMapping("/mark-as-read")
    public CommonResponse<Long> markMessagesAsRead(@RequestParam Long chatRoomId,
                                   @RequestParam Long userId) {
        chatService.markMessagesAsRead(chatRoomId, userId);
        return CommonResponse.ok(null);
    }



}