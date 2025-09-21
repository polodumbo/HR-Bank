package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Backup;
import com.codeit.HRBank.domain.BackupStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BackupRepository extends JpaRepository<Backup, Long> {

//    @Query("""
//            SELECT b FROM Backup b
//            WHERE (:worker IS NULL OR b.worker LIKE CONCAT('%', :worker, '%'))
//            AND b.startedAt >= COALESCE(:startedAtFrom, b.startedAt)
//            AND b.startedAt <= COALESCE(:startedAtTo, b.startedAt)
//            AND (:status IS NULL OR b.status = :status)
//            """)
//    Slice<List<Backup>> findByCondition2(
//            @Param("worker") String worker,
//            @Param("startedAtFrom") LocalDateTime startedAtFrom,
//            @Param("startedAtTo") LocalDateTime startedAtTo,
//            @Param("status") BackupStatus status
//    );


    @Query("""
            SELECT b FROM Backup b
            WHERE (:idAfter IS NULL OR b.id > :idAfter)
            AND b.worker LIKE '%' || COALESCE(:worker, "") || '%'
            AND b.startedAt >= COALESCE(:startedAtFrom, b.startedAt)
            AND b.startedAt <= COALESCE(:startedAtTo, b.startedAt)
            AND (:status IS NULL OR b.status = :status)
        """)
    Slice<Backup> findByCondition(
        @Param("worker") String worker,
        @Param("startedAtFrom") LocalDateTime startedAtFrom,
        @Param("startedAtTo") LocalDateTime startedAtTo,
        @Param("status") BackupStatus status,
        @Param("idAfter") Long idAfter,
        Pageable pageable
    );


    //    @Query("""
//            SELECT b FROM Backup b
//            WHERE b.status = COALESCE(:status, 'COMPLETED')
//            ORDER BY b.startedAt DESC
//            LIMIT 1
//            """)
    @Query("""
        SELECT b FROM Backup b
        WHERE b.status = 'COMPLETED'
        ORDER BY b.startedAt DESC
        LIMIT 1
        """)
    Optional<Backup> findLatest(
        @Param("status") BackupStatus status
    );

}
