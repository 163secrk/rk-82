package com.freelance.platform.service;

import com.freelance.platform.common.enums.MessageType;
import com.freelance.platform.dto.request.ChatMessageRequest;
import com.freelance.platform.dto.response.MessageVO;
import com.freelance.platform.entity.Message;
import com.freelance.platform.entity.Project;
import com.freelance.platform.entity.User;
import com.freelance.platform.exception.BusinessException;
import com.freelance.platform.repository.MessageRepository;
import com.freelance.platform.repository.ProjectRepository;
import com.freelance.platform.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageVO sendMessage(ChatMessageRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new BusinessException(404, "发送方不存在"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new BusinessException(404, "接收方不存在"));

        if (!isProjectParticipant(project, request.getSenderId())) {
            throw new BusinessException(403, "您不是该项目的参与方");
        }

        if (!isProjectParticipant(project, request.getReceiverId())) {
            throw new BusinessException(403, "接收方不是该项目的参与方");
        }

        Message message = new Message();
        BeanUtils.copyProperties(request, message);
        message.setIsRead(false);

        message = messageRepository.save(message);

        MessageVO vo = convertToVO(message);
        messagingTemplate.convertAndSend("/topic/chat/" + request.getProjectId(), vo);
        messagingTemplate.convertAndSend("/queue/notification/" + request.getReceiverId(), vo);

        return vo;
    }

    public Page<MessageVO> getMessages(Integer projectId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messagePage = messageRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable);
        return messagePage.map(this::convertToVO);
    }

    public List<MessageVO> getConversations(Integer userId) {
        List<Message> messages = messageRepository.findConversationsByUserId(userId);
        return messages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Integer messageId, Integer userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(404, "消息不存在"));

        if (!message.getReceiverId().equals(userId)) {
            throw new BusinessException(403, "您没有权限标记此消息为已读");
        }

        message.setIsRead(true);
        messageRepository.save(message);
    }

    @Transactional
    public void markAllAsRead(Integer projectId, Integer userId) {
        List<Message> messages = messageRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
        messages.stream()
                .filter(m -> m.getReceiverId().equals(userId) && !m.getIsRead())
                .forEach(m -> {
                    m.setIsRead(true);
                    messageRepository.save(m);
                });
    }

    private boolean isProjectParticipant(Project project, Integer userId) {
        return project.getClientId().equals(userId) ||
                (project.getFreelancerId() != null && project.getFreelancerId().equals(userId));
    }

    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);

        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        if (sender != null) {
            vo.setSenderName(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatar());
        }

        return vo;
    }
}
