package com.wip.workipedia.chatbot.repository;

import com.wip.workipedia.chatbot.domain.ChatbotSession;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, Long> {

    Optional<ChatbotSession> findBySessionIdAndIsDeleted(Long sessionId, String isDeleted);

    Page<ChatbotSession> findByUserIdAndIsDeletedOrderByCreatedAtDesc(
            Long userId, String isDeleted, Pageable pageable);
}
