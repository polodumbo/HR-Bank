package com.codeit.HRBank.dto.data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeTrendDto(
        LocalDate date,
        Long count,
        Long change, //증감
        double changeRate   //증감률 (이전시점 대비 %)
) {

}
