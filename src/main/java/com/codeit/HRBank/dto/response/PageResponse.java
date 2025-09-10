package com.codeit.HRBank.dto.response;

import java.util.List;

public record PageResponse<T>(
    List<T> content, // 실제데이터
    int number, // 페이지 번호
    int size, //페이지 크기
    boolean hasNext,
    Long totalElements
) {

}