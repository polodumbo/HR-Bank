package com.codeit.HRBank.service;

import com.codeit.HRBank.repository.DepartmentRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class DepartmentService {

  private final DepartmentRepository departmentRepository;

  @Transactional
  public DepartmentDto find(Long departmentId) {
    return DepartmentRepository.findBydepartmentId(departmentId)
        .map(departmentMapper::toDto)
        .orElseThrow(
            () -> new NoSuchElementException("Department with id" + departmentId + "notfound"));
  }
}
