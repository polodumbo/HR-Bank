package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Backup;

import com.codeit.HRBank.domain.BackupStatus;
import com.codeit.HRBank.dto.data.BackupDto;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BackupRepository extends JpaRepository<Backup, Long> {

    @Query("""
            SELECT b FROM Backup b 
            WHERE (:worker IS NULL OR b.worker LIKE CONCAT('%', :worker, '%'))
            AND b.startedAt >= COALESCE(:startedAtFrom, b.startedAt)
            AND b.startedAt <= COALESCE(:startedAtTo, b.startedAt)
            AND (:status IS NULL OR b.status = :status)
            """)
    List<Backup> findByCondition(
            @Param("worker") String worker,
            @Param("startedAtFrom") LocalDateTime startedAtFrom,
            @Param("startedAtTo") LocalDateTime startedAtTo,
            @Param("status") BackupStatus status
    );


    @Query("""
            SELECT b FROM Backup b
            WHERE b.status = COALESCE(:status, 'COMPLETED')
            ORDER BY b.startedAt DESC
            LIMIT 1
            """)
    Backup findLatest(
            @Param("status") BackupStatus status
    );
}
