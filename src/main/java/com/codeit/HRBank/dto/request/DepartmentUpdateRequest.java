package com.codeit.HRBank.dto.request;

import java.time.LocalDate;

public record DepartmentUpdateRequest(
    String name,
    String description,
    LocalDate establishedDate
) {

}