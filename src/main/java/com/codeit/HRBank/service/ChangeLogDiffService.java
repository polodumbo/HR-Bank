package com.codeit.HRBank.service;

import com.codeit.HRBank.domain.ChangeLog;
import com.codeit.HRBank.domain.ChangeLogDiff;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.repository.ChangeLogDiffRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeLogDiffService {

  private final ChangeLogDiffRepository changeLogDiffRepository;

  public void create(ChangeLog changeLog, Employee newEmployee) {
    List<ChangeLogDiff> diffs = new ArrayList<>();
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("name")
        .afterValue(newEmployee.getName()).build());
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("email")
        .afterValue(newEmployee.getEmail()).build());
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("department")
        .afterValue(newEmployee.getDepartment().getName()).build());
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("position")
        .afterValue(newEmployee.getPosition()).build());
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("hireDate")
        .afterValue(newEmployee.getHireDate().toString()).build());
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("status")
        .afterValue(newEmployee.getStatus().toString()).build());
    changeLogDiffRepository.saveAll(diffs);
  }

  public void delete(ChangeLog changeLog, Employee newEmployee) {
    List<ChangeLogDiff> diffs = new ArrayList<>();
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("name")
        .beforeValue(newEmployee.getName()).build());
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("email")
        .beforeValue(newEmployee.getEmail()).build());
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("department")
        .beforeValue(newEmployee.getDepartment().getName()).build());
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("position")
        .beforeValue(newEmployee.getPosition()).build());
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("hireDate")
        .beforeValue(newEmployee.getHireDate().toString()).build());
    diffs.add(ChangeLogDiff.builder().log(changeLog).propertyName("status")
        .beforeValue(newEmployee.getStatus().toString()).build());
    changeLogDiffRepository.saveAll(diffs);
  }

  public void update(ChangeLog log, Employee original, Employee updated) {
    List<ChangeLogDiff> diffs = new ArrayList<>();

    addIfChanged(diffs, log, "name",
        original.getName(), updated.getName());

    addIfChanged(diffs, log, "email",
        original.getEmail(), updated.getEmail());

    addIfChanged(diffs, log, "department",
        original.getDepartment() == null ? null : original.getDepartment().getName(),
        updated.getDepartment() == null ? null : updated.getDepartment().getName());

    addIfChanged(diffs, log, "position",
        original.getPosition(), updated.getPosition());

    addIfChanged(diffs, log, "hireDate",
        original.getHireDate(), updated.getHireDate());

    addIfChanged(diffs, log, "status",
        original.getStatus(), updated.getStatus());

    if (!diffs.isEmpty()) {
      changeLogDiffRepository.saveAll(diffs);
    }
  }

  private void addIfChanged(List<ChangeLogDiff> diffs,
      ChangeLog log,
      String property,
      Object oldVal,
      Object newVal) {
    // 값이 다를 때만 기록
    if (!Objects.equals(
        oldVal == null ? null : oldVal.toString(),
        newVal == null ? null : newVal.toString())) {
      diffs.add(ChangeLogDiff.builder()
          .log(log)
          .propertyName(property)
          .beforeValue(oldVal == null ? null : oldVal.toString())
          .afterValue(newVal == null ? null : newVal.toString())
          .build());
    }

  }
}
