package com.devops.backend.shared.email.repository;

import com.devops.backend.shared.email.entity.EmailJob;
import com.devops.backend.shared.email.enums.EmailJobType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailJobRepository extends JpaRepository<EmailJob, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM EmailJob j WHERE j.estado = 'PENDIENTE' AND j.proximoIntentoEn <= :ahora ORDER BY j.creadoEn ASC")
    List<EmailJob> findPendingJobsForProcessing(@Param("ahora") LocalDateTime ahora, Pageable pageable);

    @Query(value = """
        SELECT COUNT(*) > 0 FROM email_jobs
        WHERE tipo = 'RECORDATORIO'
        AND payload ->> 'idTicket' = CAST(:ticketId AS TEXT)
        """, nativeQuery = true)
    boolean existsRecordatorioByTicketId(@Param("ticketId") Long ticketId);
}
