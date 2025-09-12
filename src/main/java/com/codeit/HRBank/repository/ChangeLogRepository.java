package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Change_log;
<<<<<<< HEAD
import com.codeit.HRBank.domain.Change_log_diff;
import com.codeit.HRBank.dto.data.ChangeLogDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeLogRepository extends JpaRepository<Change_log, Long> {

  public Optional<Change_log> findByEmployeeNumber(String employeeNumber);
  
=======
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeLogRepository extends JpaRepository<Change_log,Long> {
    Optional<Change_log> findFirstByOrderByAtDesc();

>>>>>>> main
}
