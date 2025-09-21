package com.codeit.HRBank.dto.response;

import com.codeit.HRBank.domain.Employee;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeDetailsResponse {

    private Long id;
    private String name;
    private String email;
    private String employeeNumber;
    private Long departmentId;
    private String departmentName;
    private String position;
    private LocalDate hireDate;
    private String status;
    private Long profileImageId;

    public static EmployeeDetailsResponse from(Employee employee) {
        return EmployeeDetailsResponse.builder()
            .id(employee.getId())
            .name(employee.getName())
            .email(employee.getEmail())
            .employeeNumber(employee.getEmployeeNumber())
            .departmentId(employee.getDepartment().getId())
            .departmentName(employee.getDepartment().getName())
            .position(employee.getPosition())
            .hireDate(employee.getHireDate())
            .status(employee.getStatus().name())
            .profileImageId(
                employee.getProfileImage() != null ? employee.getProfileImage().getId() : null)
            .build();
    }
}
