package com.ani.taku_backend.chatroom.service;

import com.ani.taku_backend.chatroom.model.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.model.constant.ParticipantRole;
import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.model.document.ParticipantInfo;
import com.ani.taku_backend.chatroom.model.document.Participants;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.model.dto.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import com.ani.taku_backend.chatroom.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.repository.ChatroomMetaRepository;
import com.ani.taku_backend.chatroom.repository.ParticipantInfoRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatroomMetaRepository chatroomMetaRepository;

    private final ParticipantInfoRepository participantInfoRepository;

    @Transactional
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO requestDto) {
        validateNewChatRoom(requestDto);

        ChatRoom chatRoom = ChatRoom.builder()
                .articleId(requestDto.articleId())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 채팅방 메타정보 생성
        ChatRoomMetaInfo metaInfo = ChatRoomMetaInfo.builder()
                .chatroomId(savedRoom.getId())
                .build();
        metaInfo.initializeParticipants(requestDto.buyerId(), requestDto.sellerId());
        chatroomMetaRepository.save(metaInfo);

        return ChatRoomResponseDTO.of(savedRoom, requestDto.buyerId(), requestDto.sellerId());
    }

    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {

        List<ChatRoomMetaInfo> userChatRoomMetaInfos = chatroomMetaRepository
                .findByParticipantsUserId(userId);  // participants에 userId가 포함된 채팅방 정보만 가져옴

        // isConnected가 true인 채팅방만 필터링
        List<ChatRoomMetaInfo> connectedChatRoomMetaInfos = userChatRoomMetaInfos.stream()
                .filter(metaInfo -> metaInfo.getParticipants().getInfo().values().stream()
                        .anyMatch(participant -> participant.getIsConnected() != null
                                &&  participant.getIsConnected()))
                .collect(Collectors.toList());

        // userChatRoomMetaInfos에서 각 채팅방의 ID를 추출
        List<Long> chatRoomIds = connectedChatRoomMetaInfos.stream()
                .map(ChatRoomMetaInfo::getChatroomId)
                .collect(Collectors.toList());


        // ChatRoom에서 해당 ID들만 조회
        List<ChatRoom> userChatRooms = chatRoomRepository
                .findByIdInAndStatus(chatRoomIds, ChatRoomStatus.ACTIVE);  // 채팅방 상태가 ACTIVE인 것만 조회


        return userChatRooms.stream()
                .map(chatRoom -> {
                    ChatRoomMetaInfo chatRoomMetaInfo = chatroomMetaRepository.findById(chatRoom.getId()).get();
                    Participants participants = chatRoomMetaInfo.getParticipants();

                    Long buyerId=Long.valueOf(0);
                    Long sellerId=Long.valueOf(0);

                    for(Long key : participants.getInfo().keySet()){
                        Long id = participants.getInfo().get(key).getUserId();
                        if(participants.getInfo().get(key).getRole()==ParticipantRole.BUYER){
                            buyerId = id;
                        }else{
                            sellerId = id;
                        }
                    }

                    return new ChatRoomResponseDTO(
                            chatRoom.getId(),
                            chatRoom.getRoomId(),
                            chatRoom.getArticleId(),
                            buyerId,
                            sellerId,
                            chatRoom.getCreatedAt()
                    );}
                )
                .collect(Collectors.toList());
    }


    private void validateNewChatRoom(ChatRoomRequestDTO requestDto) {

        List<ChatRoom>  chatRooms = chatRoomRepository.findByArticleId(requestDto.articleId());



        // 기존 채팅방 정보에서 chatRoomId를 가져와 ChatRoomMeta 정보 찾기
        for (ChatRoom chatRoom : chatRooms) {
            // ChatRoomMeta 정보 찾기
            Optional<ChatRoomMetaInfo> chatRoomMetaOpt = chatroomMetaRepository.findById(chatRoom.getId());

            if (chatRoomMetaOpt.isPresent()) {
                ChatRoomMetaInfo chatRoomMeta = chatRoomMetaOpt.get();

                // ChatRoomMeta 안에 있는 Participants 정보 가져오기
                Participants participants = chatRoomMeta.getParticipants();

                for(Long key: participants.getInfo().keySet()){

                    ParticipantInfo participant = participants.getInfo().get(key);

                    // sellerId 또는 buyerId와 일치하는 userId가 있는지 확인
                    if (participant.getUserId().equals(requestDto.sellerId())) {
                        throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
                    }

                }
                // sellerId와 buyllerId를 포함한 참가자가 있는지 확인


            }
        }
    }

    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo chatRoomMetaInfo  = chatroomMetaRepository.findById(chatRoom.getId()).get();

        Participants participants = chatRoomMetaInfo.getParticipants();

        Long buyerId = 0L;
        Long sellerId = 0L;
        for(Long key: participants.getInfo().keySet()){

            if(participants.getInfo().get(key).getRole()==ParticipantRole.BUYER){
                buyerId = participants.getInfo().get(key).getUserId();
            }else{
                sellerId = participants.getInfo().get(key).getUserId();
            }
        }

        if (!participants.containsUser(userId)||buyerId==0L||sellerId==0L) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }

        return ChatRoomResponseDTO.of(chatRoom, buyerId, sellerId);
    }

    public Integer getChatRoomUnreadCount(String roomId, Long userId) {

        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId).get();
        ChatRoomMetaInfo metaInfo = chatroomMetaRepository.findById(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ParticipantInfo participantInfo = metaInfo.getParticipants().getInfo().get(userId.toString());
        if (participantInfo == null) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        return participantInfo.getMessageStock();
    }

    public Integer getTotalUnreadCount(Long userId) {
        List<ChatRoomMetaInfo> userChatrooms = chatroomMetaRepository
                .findByParticipantIdOrderByUpdateAtDesc(userId.toString());

        return userChatrooms.stream()
                .map(chatroom -> {
                    ParticipantInfo participantInfo = chatroom.getParticipants().getInfo().get(userId.toString());
                    return participantInfo != null ? participantInfo.getMessageStock() : 0;
                })
                .reduce(0, Integer::sum);
    }
}