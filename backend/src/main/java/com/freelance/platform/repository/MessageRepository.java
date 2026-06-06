package com.freelance.platform.repository;

import com.freelance.platform.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    Page<Message> findByProjectIdOrderByCreatedAtDesc(Integer projectId, Pageable pageable);

    List<Message> findByProjectIdOrderByCreatedAtAsc(Integer projectId);

    @Query("SELECT m FROM Message m WHERE m.receiverId = :receiverId AND m.isRead = false")
    List<Message> findUnreadByReceiverId(@Param("receiverId") Integer receiverId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :receiverId AND m.isRead = false")
    long countUnreadByReceiverId(@Param("receiverId") Integer receiverId);

    @Query("SELECT m FROM Message m WHERE (m.senderId = :userId OR m.receiverId = :userId) " +
           "GROUP BY m.projectId ORDER BY m.createdAt DESC")
    List<Message> findConversationsByUserId(@Param("userId") Integer userId);
}
