package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.ChangeLogType;
import com.codeit.HRBank.domain.ChangeLog;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {

  public Optional<ChangeLog> findByEmployeeNumber(String employeeNumber);

  Optional<ChangeLog> findFirstByOrderByAtDesc();

  @Query("""
        SELECT COUNT(c) FROM ChangeLog c
        WHERE c.at >= COALESCE(:fromDateTime, c.at)
            AND c.at <=  COALESCE(:toDateTime, c.at)
      """)
  Long countByDate(
      @Param("fromDateTime") LocalDateTime fromDateTime,
      @Param("toDateTime") LocalDateTime toDateTime);

  @Query("""
          SELECT c FROM ChangeLog c
          WHERE (:idAfter IS NULL OR c.id > :idAfter)
          AND c.employeeNumber LIKE '%' || COALESCE(:employeeNumber, "") || '%'
          AND c.memo LIKE '%' || COALESCE(:memo, "") || '%'
          AND c.ipAddress LIKE '%' || COALESCE(:ipAddress, "") || '%'
          AND c.at >= COALESCE(:fromDateTime, c.at)
          AND c.at <= COALESCE(:toDateTime, c.at)
          AND (:type IS NULL OR c.type = :type)
      """)
  Slice<ChangeLog> findByCondition(
      @Param("employeeNumber") String employeeNumber,
      @Param("type") ChangeLogType type,
      @Param("memo") String memo,
      @Param("ipAddress") String ipAddress,
      @Param("fromDateTime") LocalDateTime atFrom,
      @Param("toDateTime") LocalDateTime atTo,
      @Param("idAfter") Long idAfter,
      Pageable pageable
  );


}
