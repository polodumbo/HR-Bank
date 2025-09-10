package com.codeit.HRBank.mapper;

import com.codeit.HRBank.domain.Department;
import com.codeit.HRBank.dto.data.DepartmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepartmentMapper {

  public DepartmentDto toDto(Department department) {
    return new DepartmentDto(
        department.getId(),
        department.getName(),
        department.getDescription(),
        department.getEstablishedDate()
        // department.getEmployeeCount() // 구현 전 컴파일 에러 방지 주석
    );
  }
}