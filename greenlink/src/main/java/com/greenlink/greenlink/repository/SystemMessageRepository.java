package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.SystemMessage;
import com.greenlink.greenlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemMessageRepository extends JpaRepository<SystemMessage, Long> {
    List<SystemMessage> findByRecipientOrderByCreatedAtDesc(User recipient);
    List<SystemMessage> findByRecipientAndReadOrderByCreatedAtDesc(User recipient, boolean read);
    long countByRecipientAndRead(User recipient, boolean read);
}
