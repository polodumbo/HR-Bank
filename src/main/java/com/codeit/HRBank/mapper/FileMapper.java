package com.codeit.HRBank.mapper;

import com.codeit.HRBank.domain.File;
import com.codeit.HRBank.dto.data.FileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileMapper {

  public FileDto toDto(File file) {
    return new FileDto(
        file.getId(),
        file.getFileName(),
        file.getSize(),
        file.getContentType()
    );
  }
}
