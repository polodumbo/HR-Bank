package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Change_log;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeLogRepository extends JpaRepository<Change_log,Long> {
}
