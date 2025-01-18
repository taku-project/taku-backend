package com.ani.taku_backend.chatroom.controller;

import com.ani.taku_backend.chatroom.model.dto.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.service.ChatRoomService;
import com.ani.taku_backend.common.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

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

    @GetMapping("/{roomId}")
    public CommonResponse<ChatRoomResponseDTO> getChatRoom(
            @PathVariable String roomId,
            @RequestParam Long userId) {
        ChatRoomResponseDTO chatRoom = chatRoomService.findChatRoom(roomId, userId);
        return CommonResponse.ok(chatRoom);
    }
}