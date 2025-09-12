package com.codeit.HRBank.mapper;

import com.codeit.HRBank.domain.Change_log;
import com.codeit.HRBank.dto.data.ChangeLogDto;
import com.codeit.HRBank.dto.response.CursorPageResponseChangeLogDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class ChangeLogMapper {

  public ChangeLogDto toDto(Change_log changeLog) {
    if (changeLog == null) {
      return null;
    }
    return new ChangeLogDto(
        changeLog.getId(),
        changeLog.getType().name(),
        changeLog.getEmployeeNumber(),
        changeLog.getMemo(),
        changeLog.getIpAddress() == null ? null : changeLog.getIpAddress().toString(),
        changeLog.getAt().toString()
    );
  }

  // Slice<ChangeLog>을 CursorPageResponseChangeLogDto<ChangeLogDto>로 변환하는 메서드
  public CursorPageResponseChangeLogDto<ChangeLogDto> toDtoSlice(
      Slice<Change_log> changeLogSlice) {

    // 1. Slice<Change_log>의 content를 Stream을 이용해 List<ChangeLogDto>로 변환
    List<ChangeLogDto> dtoList = changeLogSlice.getContent().stream()
        .map(this::toDto)
        .collect(Collectors.toList());

    // 2. CursorPageResponseChangeLogDto 에 필요한 커서 및 페이지 정보 계산
    Long nextIdAfter = null;
    if (changeLogSlice.hasContent() && changeLogSlice.hasNext()) {
      // 변환된 DTO 리스트의 마지막 요소에서 ID 값을 추출
      ChangeLogDto lastDto = dtoList.get(dtoList.size() - 1);
      nextIdAfter = lastDto.id();
    }

    // 3. 변환된 DTO 리스트와 커서 정보를 담아 최종 응답 객체 생성
    return new CursorPageResponseChangeLogDto<>(
        dtoList,
        null, // nextCursor는 필요시 추가
        nextIdAfter,
        changeLogSlice.getSize(),
        (int) changeLogSlice.getNumberOfElements(),
        changeLogSlice.hasNext()
    );
  }

}

