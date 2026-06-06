package com.freelance.platform.controller;

import com.freelance.platform.dto.request.ChatMessageRequest;
import com.freelance.platform.dto.response.MessageVO;
import com.freelance.platform.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request) {
        messageService.sendMessage(request);
    }
}
