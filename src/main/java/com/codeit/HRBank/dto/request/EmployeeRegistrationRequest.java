package com.codeit.HRBank.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeRegistrationRequest {

  @NotNull(message = "이름은 필수 입력 항목입니다.")
  private String name;

  @NotNull(message = "이메일은 필수 입력 항목입니다.")
  @Email(message = "유효한 이메일 형식이 아닙니다.")
  private String email;

  @NotNull(message = "부서는 필수 입력 항목입니다.")
  private String departmentName;

  @NotNull(message = "직함은 필수 입력 항목입니다.")
  private String position;

  @NotNull(message = "입사일은 필수 입력 항목입니다.")
  private LocalDate hireDate;
}
