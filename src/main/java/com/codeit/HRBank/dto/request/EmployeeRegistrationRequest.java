package com.codeit.HRBank.dto.request;

import java.time.LocalDate;

public record EmployeeRegistrationRequest(
    String name,
    String email,
    Long departmentId,
    String position,
    LocalDate hireDate
) {

}
