package com.codeit.HRBank.dto.request;

public record FileCreateRequest(
    String fileName,
    String contentType,
    byte[] bytes
) {

}
