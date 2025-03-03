package com.ani.taku_backend.chatroom.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

public class StompController {
    @MessageMapping("{/roomId}")
    @SendTo("/sub/{roomID}")
    public void sendMessage(@DestinationVariable Long roomID, String message) {

    }

}
