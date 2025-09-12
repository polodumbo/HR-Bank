package com.codeit.HRBank.mapper;

import com.codeit.HRBank.domain.Department;
import com.codeit.HRBank.dto.data.DepartmentDto;
import com.codeit.HRBank.dto.response.CursorPageResponseDepartmentDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepartmentMapper {

  public DepartmentDto toDto(Department department) {
    return new DepartmentDto(
        department.getId(),
        department.getName(),
        department.getDescription(),
        department.getEstablishedDate()
        // department.getEmployeeCount() // 구현 전 컴파일 에러 방지 주석
    );
  }



    // Slice<Department>을 CursorPageResponseDepartmentDto<DepartmentDto>로 변환하는 메서드
    public CursorPageResponseDepartmentDto<DepartmentDto> toDtoSlice(Slice<Department> departmentSlice) {

        // 1. Slice<Department>의 content를 Stream을 이용해 List<DepartmentDto>로 변환
        List<DepartmentDto> dtoList = departmentSlice.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        // 2. CursorPageResponseDepartmentDto에 필요한 커서 및 페이지 정보 계산
        Long nextIdAfter = null;
        if (departmentSlice.hasContent() && departmentSlice.hasNext()) {
            // 변환된 DTO 리스트의 마지막 요소에서 ID 값을 추출
            DepartmentDto lastDto = dtoList.get(dtoList.size() - 1);
            nextIdAfter = lastDto.id();
        }

        // 3. 변환된 DTO 리스트와 커서 정보를 담아 최종 응답 객체 생성
        return new CursorPageResponseDepartmentDto<>(
                dtoList,
                null, // nextCursor는 필요시 추가
                nextIdAfter,
                departmentSlice.getSize(),
                (int) departmentSlice.getNumberOfElements(),
                departmentSlice.hasNext()
        );
    }


}