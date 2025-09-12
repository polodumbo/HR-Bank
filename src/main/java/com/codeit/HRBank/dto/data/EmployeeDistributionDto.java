package com.codeit.HRBank.dto.data;

public record EmployeeDistributionDto(
        String groupKey,    //부서명
        Long count,  //직원수
        double percentage   //비율%
) {

}
