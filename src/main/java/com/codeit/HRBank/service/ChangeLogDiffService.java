package com.codeit.HRBank.service;

import com.codeit.HRBank.domain.Change_log;
import com.codeit.HRBank.domain.Change_log_diff;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.dto.data.DiffDto;
import com.codeit.HRBank.repository.ChangeLogDiffRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeLogDiffService {

  private final ChangeLogDiffRepository changeLogDiffRepository;

  public void create(Change_log changeLog, Employee newEmployee) {
    List<Change_log_diff> diffs = new ArrayList<>();
    diffs.add(Change_log_diff.builder().log(changeLog).propertyName("name")
        .afterValue(newEmployee.getName()).build());
    diffs.add(Change_log_diff.builder().log(changeLog).propertyName("email")
        .afterValue(newEmployee.getEmail()).build());
    diffs.add(Change_log_diff.builder().log(changeLog).propertyName("department")
        .afterValue(newEmployee.getDepartment().getName()).build());
    diffs.add(Change_log_diff.builder().log(changeLog).propertyName("position")
        .afterValue(newEmployee.getPosition()).build());
    diffs.add(Change_log_diff.builder().log(changeLog).propertyName("hireDate")
        .afterValue(newEmployee.getHireDate().toString()).build());
    diffs.add(Change_log_diff.builder().log(changeLog).propertyName("status")
        .afterValue(newEmployee.getStatus().toString()).build());
    changeLogDiffRepository.saveAll(diffs);
  }
}
