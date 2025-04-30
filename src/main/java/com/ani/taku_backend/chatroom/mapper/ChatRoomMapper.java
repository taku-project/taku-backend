package com.ani.taku_backend.chatroom.mapper;

import com.ani.taku_backend.chatroom.dto.ChatRoomAggregateResult;
import com.ani.taku_backend.chatroom.dto.response.ChatRoomResDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * 채팅방 관련 DTO 변환을 위한 MapStruct 매퍼
 */
@Mapper(componentModel = "spring")
public interface ChatRoomMapper {
    
    @Mapping(target = "chatRoomId", source = "result.chatRoom.id")
    @Mapping(target = "wsRoomId", source = "result.chatRoom.wsRoomId")
    @Mapping(target = "articleId", source = "result.chatRoom.articleId")
    @Mapping(target = "buyerId", source = "result.buyer.userId")
    @Mapping(target = "sellerId", source = "result.seller.userId")
    @Mapping(target = "buyerNickname", source = "result.buyer.nickname")
    @Mapping(target = "sellerNickname", source = "result.seller.nickname")
    @Mapping(target = "lastMessage", expression = "java(null)")
    @Mapping(target = "createdAt", source = "result.chatRoom.createdAt")
    @Mapping(target = "updatedAt", source = "result.chatRoom.updatedAt")
    @Mapping(target = "articleImageUrl", source = "result.articleImage")
    @Mapping(target = "unreadMessageCount", constant = "0")
    @Mapping(target = "buyerProfileImageUrl", source = "result.buyer.profileImg")
    @Mapping(target = "sellerProfileImageUrl", source = "result.seller.profileImg")
    @Mapping(target = "articleName", source = "result.articleTitle")
    @Mapping(target = "articlePrice", source = "result.articlePrice")
    ChatRoomResDTO toChatRoomResponseDTO(ChatRoomAggregateResult result);
} 