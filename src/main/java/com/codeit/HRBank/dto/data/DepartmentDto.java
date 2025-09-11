package com.codeit.HRBank.dto.data;

import java.time.LocalDateTime;

public record DepartmentDto(
    Long id,
    String name,
    String description,
    LocalDateTime establishedDate
    // private Long employeeCount; // 구현 전 컴파일 에러 방지 주석
) {

}