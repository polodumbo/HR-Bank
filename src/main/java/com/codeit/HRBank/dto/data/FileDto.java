package com.codeit.HRBank.dto.data;

public record FileDto(
    Long id,
    String fileName,
    Long size,
    String contentType
) {

}
