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
                .buyerId(requestDto.buyerId())
                .sellerId(requestDto.sellerId())
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

        // userChatRoomMetaInfos에서 각 채팅방의 ID를 추출
        List<Long> chatRoomIds = userChatRoomMetaInfos.stream()
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
                        if(participants.getInfo().get(key).getRole()==ParticipantRole.BUYER){
                            buyerId = participants.getInfo().get(key).getUserId();
                        }else{
                            sellerId = participants.getInfo().get(key).getUserId();
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


    /*
      [유의]
    * 기존 buyerId와 sellerId와 articleId를 이용해 찾는 것으로 되어 있었으나, buyerId sellerId 속성이 없어지며 ArticleId만 남겨두었습니다.
    * 별도로 고려해야 하는 것이라면 수정 부탁드립니다.
    * */
    private void validateNewChatRoom(ChatRoomRequestDTO requestDto) {
        if (chatRoomRepository.existsByArticleId(
                requestDto.articleId())) {
            throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
        }
    }

    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMetaInfo chatRoomMetaInfo  = chatroomMetaRepository.findById(chatRoom.getId()).get();

        Participants participants = chatRoomMetaInfo.getParticipants();

        Long buyllerId = participantInfoRepository.findByRole(ParticipantRole.BUYER).getUserId();
        Long sellerId = participantInfoRepository.findByRole(ParticipantRole.SELLER).getUserId();


        if (!participants.containsUser(userId)) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        return ChatRoomResponseDTO.of(chatRoom, buyllerId, sellerId);
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