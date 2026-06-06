package com.freelance.platform.controller;

import com.freelance.platform.common.Result;
import com.freelance.platform.dto.response.MessageVO;
import com.freelance.platform.entity.User;
import com.freelance.platform.security.CustomUserDetailsService;
import com.freelance.platform.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/conversations")
    public Result<List<MessageVO>> getConversations() {
        Integer userId = getCurrentUserId();
        List<MessageVO> conversations = messageService.getConversations(userId);
        return Result.success(conversations);
    }

    @GetMapping("/{projectId}")
    public Result<Page<MessageVO>> getMessages(
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<MessageVO> messages = messageService.getMessages(projectId, page, size);
        messageService.markAllAsRead(projectId, getCurrentUserId());
        return Result.success(messages);
    }

    @PutMapping("/{messageId}/read")
    public Result<Void> markAsRead(@PathVariable Integer messageId) {
        Integer userId = getCurrentUserId();
        messageService.markAsRead(messageId, userId);
        return Result.success();
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            String email = ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername();
            User user = userDetailsService.loadUserEntityByUsername(email);
            return user.getId();
        }
        throw new RuntimeException("未登录");
    }
}
