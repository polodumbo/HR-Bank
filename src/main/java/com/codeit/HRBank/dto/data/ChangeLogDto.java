package com.codeit.HRBank.dto.data;

public record ChangeLogDto(
    Long id,
    String type,
    String employeeNumber,
    String memo,
    String ipAddress,
    String at
) {

}
