package com.devops.backend.shared.email.repository;

import com.devops.backend.shared.email.entity.EmailJobAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailJobAttachmentRepository extends JpaRepository<EmailJobAttachment, Long> {
}
