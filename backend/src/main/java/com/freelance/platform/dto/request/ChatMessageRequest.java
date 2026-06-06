package com.freelance.platform.dto.request;

import com.freelance.platform.common.enums.MessageType;
import lombok.Data;

@Data
public class ChatMessageRequest {
    private Integer projectId;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private MessageType type = MessageType.TEXT;
    private String fileUrl;
}
