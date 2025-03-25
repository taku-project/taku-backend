package com.ani.taku_backend.chatroom.controller;

import com.ani.taku_backend.chatroom.dto.request.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.dto.response.ChatMessageListResponseDTO;
import com.ani.taku_backend.chatroom.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.service.ChatRoomService;
import com.ani.taku_backend.chatroom.service.ChatMessageService;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.util.List;


@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
@Tag(name = "채팅방 API", description = "채팅방 생성, 조회, 관리 API")
public class ChatRoomController {

    private final ChatRoomService chatRoomFacadeService;
    private final ChatMessageService chatMessageFacadeService;

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
        ChatRoomResponseDTO responseDto = chatRoomFacadeService.createChatRoom(requestDto);
        return CommonResponse.created(responseDto);
    }

    /**
     * 현재 사용자의 채팅방 목록을 조회합니다.
     *
     * @param page 페이지 번호 (기본값: 0)
     * @param size 조회할 채팅방 개수 (기본값: 20)
     * @param principalUser 현재 인증된 사용자
     * @return 사용자의 채팅방 목록
     */
    @Operation(
            summary = "채팅방 목록 가져오기", 
            description = "사용자가 참여한 모든 채팅방 목록 조회 API입니다. 채팅방 정보, 마지막 메시지, 읽지 않은 메시지 수 등을 포함합니다. 무한 스크롤을 위한 페이징을 지원합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomResponseDTO.class))
            )
    })
    @GetMapping
    public CommonResponse<Slice<ChatRoomResponseDTO>> getChatRoomList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        // 업데이트 시간 내림차순, ID 내림차순으로 정렬
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt", "id"));
        Slice<ChatRoomResponseDTO> chatRooms = chatRoomFacadeService.findChatRoomListWithSlice(
                principalUser.getUserId(), pageable);
        return CommonResponse.ok(chatRooms);
    }

    /**
     * 특정 채팅방의 상세 정보를 조회합니다.
     *
     * @param wsRoomId 채팅방의 WebSocket ID
     * @param principalUser 현재 인증된 사용자
     * @return 해당 채팅방의 상세 정보
     */
    @Operation(
            summary = "특정 채팅방 조회",
            description = "채팅방 ID로 특정 채팅방의 상세 정보를 조회합니다. 마지막 메시지, 읽지 않은 메시지 수, 참여자 정보 등을 포함합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 채팅방",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            )
    })
    @GetMapping("/{wsRoomId}")
    public CommonResponse<ChatRoomResponseDTO> getChatRoom(
            @PathVariable("wsRoomId") String wsRoomId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        ChatRoomResponseDTO chatRoom = chatRoomFacadeService.findChatRoom(wsRoomId, principalUser.getUserId());
        return CommonResponse.ok(chatRoom);
    }

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
        chatMessageFacadeService.leaveRoom(wsRoomId, principalUser.getUserId());
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
        chatMessageFacadeService.markMessagesAsRead(wsRoomId, principalUser.getUserId());
        return CommonResponse.ok(null);
    }

    /**
     * 사용자의 모든 채팅방에서 안읽은 메시지의 총 개수를 조회합니다.
     *
     * @param principalUser 현재 인증된 사용자
     * @return 안읽은 메시지 총 개수
     */
    @Operation(summary = "총 안읽음 메시지 갯수 반환", description = "사용자의 모든 채팅방에서 안읽은 메시지의 총 개수를 반환합니다.")
    @GetMapping("/unread/total")
    public CommonResponse<Integer> getTotalUnreadCount(
            @AuthenticationPrincipal PrincipalUser principalUser) {
        Integer totalUnreadCount = chatRoomFacadeService.getTotalUnreadCount(principalUser.getUserId());
        return CommonResponse.ok(totalUnreadCount);
    }

    /**
     * 특정 채팅방의 메시지 이력을 조회합니다.
     * 무한 스크롤을 위한 파라미터를 지원합니다.
     *
     * @param wsRoomId 채팅방의 WebSocket ID
     * @param messageId 이 메시지 ID보다 이전 메시지를 조회 (첫 로드 시 null)
     * @param limit 조회할 메시지 개수 (기본값: 30)
     * @param principalUser 현재 인증된 사용자
     * @return 메시지 목록과 무한 스크롤 정보
     */
    @Operation(summary = "채팅방 메시지 이력 조회", description = "무한 스크롤을 위한 API입니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "메시지 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatMessageListResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 채팅방",
                    content = @Content(schema = @Schema(implementation = ErrorCode.class))
            )
    })
    @GetMapping("/{wsRoomId}/messages")
    public CommonResponse<ChatMessageListResponseDTO> getChatMessages(
            @PathVariable("wsRoomId") String wsRoomId,
            @RequestParam(name = "messageId", required = false) String messageId,
            @RequestParam(name = "limit", defaultValue = "30") int limit,
            @AuthenticationPrincipal PrincipalUser principalUser) {

        // 권한 검사 (사용자가 해당 채팅방에 접근 권한이 있는지 확인)
        chatMessageFacadeService.validateChatRoomAccess(wsRoomId, principalUser.getUserId());

        // 메시지 이력 조회
        ChatMessageListResponseDTO messages = chatMessageFacadeService.getChatMessages(wsRoomId, messageId, limit);

        return CommonResponse.ok(messages);
    }

    /**
     * 사용자가 판매자로 참여한 채팅방 목록을 조회합니다.
     */
    @Operation(
            summary = "판매 중인 채팅방 목록 가져오기", 
            description = "사용자가 판매자로 참여한 채팅방 목록 조회 API입니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomResponseDTO.class))
            )
    })
    @GetMapping("/selling")
    public CommonResponse<List<ChatRoomResponseDTO>> getSellingChatRooms(
            @AuthenticationPrincipal PrincipalUser principalUser) {
        List<ChatRoomResponseDTO> chatRooms = chatRoomFacadeService.findChatRoomListByRole(
                principalUser.getUserId(), JangterChatRole.SELLER);
        return CommonResponse.ok(chatRooms);
    }

    /**
     * 사용자가 구매자로 참여한 채팅방 목록을 조회합니다.
     */
    @Operation(
            summary = "구매 중인 채팅방 목록 가져오기", 
            description = "사용자가 구매자로 참여한 채팅방 목록 조회 API입니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatRoomResponseDTO.class))
            )
    })
    @GetMapping("/buying")
    public CommonResponse<List<ChatRoomResponseDTO>> getBuyingChatRooms(
            @AuthenticationPrincipal PrincipalUser principalUser) {
        List<ChatRoomResponseDTO> chatRooms = chatRoomFacadeService.findChatRoomListByRole(
                principalUser.getUserId(), JangterChatRole.BUYER);
        return CommonResponse.ok(chatRooms);
    }
}