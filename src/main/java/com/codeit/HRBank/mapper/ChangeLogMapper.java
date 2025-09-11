package com.codeit.HRBank.mapper;

import com.codeit.HRBank.domain.Change_log;
import com.codeit.HRBank.dto.data.ChangeLogDto;
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
        changeLog.getIp_address(),
        changeLog.getAt().toString()
    );
  }
}

