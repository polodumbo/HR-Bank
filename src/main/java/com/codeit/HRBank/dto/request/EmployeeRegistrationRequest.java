package com.codeit.HRBank.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

public record EmployeeRegistrationRequest(
    String name,
    String email,
    Long departmentId,
    String position,
    LocalDate hireDate
) {}
