package com.codeit.HRBank.mapper;

import com.codeit.HRBank.domain.Department;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.dto.data.DepartmentDto;
import com.codeit.HRBank.dto.data.EmployeeDto;
import com.codeit.HRBank.dto.response.CursorPageResponseDepartmentDto;
import com.codeit.HRBank.dto.response.CursorPageResponseEmployeeDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeMapper {

    public EmployeeDto toDto(Employee employee) {

        Long profileImageId = null;
        if (employee.getProfileImage() != null) {
            profileImageId = employee.getProfileImage().getId();
        }

        return new EmployeeDto(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getEmployeeNumber(),
                employee.getDepartment().getId(),
                employee.getDepartment().getName(),
                employee.getPosition(),
                employee.getHireDate(),
                employee.getStatus(),
                profileImageId
        );
    }


    // Slice<Employee>을 CursorPageResponseEmployeeDto<EmployeeDto>로 변환하는 메서드
    public CursorPageResponseEmployeeDto<EmployeeDto> toDtoSlice(
            Slice<Employee> employeeSlice) {

        // 1. Slice<Employee>의 content를 Stream을 이용해 List<EmployeeDto>로 변환
        List<EmployeeDto> dtoList = employeeSlice.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        // 2. CursorPageResponseEmployeeDto에 필요한 커서 및 페이지 정보 계산
        Long nextIdAfter = null;
        if (employeeSlice.hasContent() && employeeSlice.hasNext()) {
            // 변환된 DTO 리스트의 마지막 요소에서 ID 값을 추출
            EmployeeDto lastDto = dtoList.get(dtoList.size() - 1);
            nextIdAfter = lastDto.id();
        }

        // 3. 변환된 DTO 리스트와 커서 정보를 담아 최종 응답 객체 생성
        return new CursorPageResponseEmployeeDto<>(
                dtoList,
                null, // nextCursor는 필요시 추가
                nextIdAfter,
                employeeSlice.getSize(),
                (int) employeeSlice.getNumberOfElements(),
                employeeSlice.hasNext()
        );
    }


}