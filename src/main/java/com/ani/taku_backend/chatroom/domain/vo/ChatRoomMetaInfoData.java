package com.ani.taku_backend.chatroom.domain.vo;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import java.util.List;


public class ChatRoomMetaInfoData {
    private final ChatRoomMetaInfos metaInfos;
    private final ChatRoomMessages lastMessages;
    private final UnreadMessageCounts unreadCounts;

    public ChatRoomMetaInfoData(
            ChatRoomMetaInfos metaInfos,
            ChatRoomMessages lastMessages,
            UnreadMessageCounts unreadCounts) {
        this.metaInfos = metaInfos;
        this.lastMessages = lastMessages;
        this.unreadCounts = unreadCounts;
    }
    

    public ChatRoomMetaInfoData(
            List<ChatRoomMetaInfo> metaInfoList,
            ChatRoomMessages lastMessages,
            UnreadMessageCounts unreadCounts) {
        this(ChatRoomMetaInfos.of(metaInfoList), lastMessages, unreadCounts);
    }


    public static ChatRoomMetaInfoData empty() {
        return new ChatRoomMetaInfoData(
            ChatRoomMetaInfos.empty(), 
            ChatRoomMessages.empty(), 
            UnreadMessageCounts.empty()
        );
    }

    public ChatRoomMetaInfos getMetaInfos() {
        return metaInfos;
    }

    public ChatRoomMessages getLastMessages() {
        return lastMessages;
    }

    public UnreadMessageCounts getUnreadCounts() {
        return unreadCounts;
    }
} 