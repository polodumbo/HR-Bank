package com.codeit.HRBank.dto.data;

import java.time.LocalDate;

public record DepartmentDto(
    Long id,
    String name,
    String description,
    LocalDate establishedDate
    // private Long employeeCount; // 구현 전 컴파일 에러 방지 주석
) {

}