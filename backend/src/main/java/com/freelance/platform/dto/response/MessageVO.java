package com.freelance.platform.dto.response;

import com.freelance.platform.common.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Integer id;
    private Integer projectId;
    private Integer senderId;
    private String senderName;
    private String senderAvatar;
    private Integer receiverId;
    private String content;
    private MessageType type;
    private String fileUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
