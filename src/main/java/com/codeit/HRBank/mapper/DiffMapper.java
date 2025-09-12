package com.codeit.HRBank.mapper;

import com.codeit.HRBank.domain.Change_log_diff;
import com.codeit.HRBank.dto.data.ChangeLogDto;
import com.codeit.HRBank.dto.data.DiffDto;
import org.springframework.stereotype.Component;

@Component
public class DiffMapper {

  public DiffDto toDto(Change_log_diff changeLogDiff) {
    if (changeLogDiff == null) {
      return null;
    }

    return new DiffDto(
        changeLogDiff.getPropertyName(),
        changeLogDiff.getBeforeValue(),
        changeLogDiff.getAfterValue()
    );
  }
}

