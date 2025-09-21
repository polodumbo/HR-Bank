package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.ChangeLogDiff;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeLogDiffRepository extends JpaRepository<ChangeLogDiff, Long> {

  List<ChangeLogDiff> findAllByLog_Id(Long logId);
}
