package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.ChangeLogType;
import com.codeit.HRBank.domain.Change_log;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChangeLogRepository extends JpaRepository<Change_log, Long> {

  public Optional<Change_log> findByEmployeeNumber(String employeeNumber);

  Optional<Change_log> findFirstByOrderByAtDesc();

  @Query("""
        SELECT COUNT(c) FROM Change_log c
        WHERE (c.at >= :fromDate)
            AND (c.at <  :toDate)
      """)
  Long countByDate(
      @Param("fromDate") LocalDate fromDate,
      @Param("toDate") LocalDate toDate);

  @Query("""
          SELECT c FROM Change_log c
          WHERE (:idAfter IS NULL OR c.id > :idAfter)
          AND c.employeeNumber LIKE '%' || COALESCE(:employeeNumber, "") || '%'
          AND c.memo LIKE '%' || COALESCE(:memo, "") || '%'
          AND c.ipAddress LIKE '%' || COALESCE(:ipAddress, "") || '%'
          AND c.at >= COALESCE(:atFrom, c.at)
          AND c.at <= COALESCE(:atTo, c.at)
          AND (:type IS NULL OR c.type = :type)
      """)
  Slice<Change_log> findByCondition(
      @Param("employeeNumber") String employeeNumber,
      @Param("type") ChangeLogType type,
      @Param("memo") String memo,
      @Param("ipAddress") String ipAddress,
      @Param("atFrom") LocalDate atFrom,
      @Param("atTo") LocalDate atTo,
      @Param("idAfter") Long idAfter,
      Pageable pageable
  );
}
