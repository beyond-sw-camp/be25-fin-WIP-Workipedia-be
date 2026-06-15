package com.wip.workipedia.chatbot.repository;

import com.wip.workipedia.chatbot.domain.ChatbotMessage;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {

    List<ChatbotMessage> findTop10BySessionIdAndIsDeletedOrderByCreatedAtDesc(
            Long sessionId, String isDeleted);

    Page<ChatbotMessage> findBySessionIdAndIsDeletedOrderByCreatedAtAsc(
            Long sessionId, String isDeleted, Pageable pageable);
}
