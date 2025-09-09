package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Change_log_diff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeLogDiffRepository extends JpaRepository<Change_log_diff,Long> {
}
