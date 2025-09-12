package com.codeit.HRBank.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DepartmentUpdateRequest(
    String name,
    String description,
    LocalDate establishedDate
) {

}