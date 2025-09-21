package com.codeit.HRBank.dto.data;

import com.codeit.HRBank.domain.EmploymentStatus;
import java.time.LocalDate;

public record EmployeeDto(
    Long id,
    String name,
    String email,
    String employeeNumber,
    Long departmentId,
    String departmentName,
    String position,
    LocalDate hireDate,
    EmploymentStatus status,
    Long profileImageId
) {

}
