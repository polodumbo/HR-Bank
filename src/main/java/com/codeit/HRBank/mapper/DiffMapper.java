package com.codeit.HRBank.mapper;

import com.codeit.HRBank.domain.ChangeLogDiff;
import com.codeit.HRBank.dto.data.DiffDto;
import org.springframework.stereotype.Component;

@Component
public class DiffMapper {

  public DiffDto toDto(ChangeLogDiff changeLogDiff) {
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

