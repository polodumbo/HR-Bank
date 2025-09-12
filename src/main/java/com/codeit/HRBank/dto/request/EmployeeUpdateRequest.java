package com.codeit.HRBank.dto.request;

import com.codeit.HRBank.domain.EmploymentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
  private EmploymentStatus status;
  private String memo;
}
