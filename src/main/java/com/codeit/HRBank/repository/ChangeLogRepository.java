package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Change_log;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeLogRepository extends JpaRepository<Change_log, Long> {

  public Optional<Change_log> findByEmployeeNumber(String employeeNumber);

  Optional<Change_log> findFirstByOrderByAtDesc();

}
