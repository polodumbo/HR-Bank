package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Change_log;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChangeLogRepository extends JpaRepository<Change_log, Long> {

  public Optional<Change_log> findByEmployeeNumber(String employeeNumber);

  Optional<Change_log> findFirstByOrderByAtDesc();

  @Query("""
        SELECT COUNT(c) FROM Change_log c
        WHERE (:from IS NULL OR c.at >= :from)
        AND (:to IS NULL OR c.at <= :to)
      """)
  Long countByDate(
      @Param("fromDate") LocalDate from,
      @Param("toDate") LocalDate to);

}
