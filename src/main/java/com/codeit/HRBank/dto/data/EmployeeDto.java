package com.codeit.HRBank.dto.data;

import com.codeit.HRBank.domain.EmploymentStatus;
import java.time.LocalDateTime;

public record EmployeeDto(
        Long id,
        String name,
        String email,
        String employeeNumber,
        Long departmentId,
        String departmentName,
        String position,
        LocalDateTime hireDate,
        EmploymentStatus status,
        Long profileImageId
) {

}
