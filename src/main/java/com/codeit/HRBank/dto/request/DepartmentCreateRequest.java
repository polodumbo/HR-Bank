package com.codeit.HRBank.dto.request;

import java.time.LocalDateTime;

public record DepartmentCreateRequest(
    String name,
    String description,
    LocalDateTime establishedDate
) {

}