package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.MessageType;
import com.greenlink.greenlink.model.SystemMessage;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.SystemMessageRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemMessageService {

    @Autowired
    private SystemMessageRepository systemMessageRepository;

    @Transactional
    public SystemMessage sendSystemMessage(User recipient, String content, String type) {
        MessageType messageType;
        try {
            messageType = MessageType.valueOf(type);
        } catch (IllegalArgumentException e) {
            messageType = MessageType.SYSTEM;
        }

        SystemMessage message = new SystemMessage(recipient, content, messageType);
        return systemMessageRepository.save(message);
    }

    @Transactional
    public void markAsRead(Long messageId) {
        systemMessageRepository.findById(messageId).ifPresent(message -> {
            message.setRead(true);
            systemMessageRepository.save(message);
        });
    }

    @Transactional
    public void markAllAsRead(User user) {
        systemMessageRepository.findByRecipientAndReadOrderByCreatedAtDesc(user, false)
            .forEach(message -> {
                message.setRead(true);
                systemMessageRepository.save(message);
            });
    }
    
    public List<SystemMessage> getUnreadMessages(User user) {
        return systemMessageRepository.findByRecipientAndReadOrderByCreatedAtDesc(user, false);
    }
    
    public long countUnreadMessages(User user) {
        return systemMessageRepository.countByRecipientAndRead(user, false);
    }
}
