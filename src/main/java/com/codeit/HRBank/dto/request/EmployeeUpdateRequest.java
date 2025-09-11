package com.codeit.HRBank.dto.request;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeUpdateRequest {
    private String name;
    private String email;
    private Long departmentId;
    private String position;
    private LocalDate hireDate;
    private Long profileImageId;
}
