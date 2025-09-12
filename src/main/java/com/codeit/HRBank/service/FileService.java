package com.codeit.HRBank.service;

import com.codeit.HRBank.domain.File;
import com.codeit.HRBank.dto.data.FileDto;
import com.codeit.HRBank.dto.request.FileCreateRequest;
import com.codeit.HRBank.mapper.FileMapper;
import com.codeit.HRBank.repository.FileRepository;
import com.codeit.HRBank.storage.FileStorage;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileService {

  private final FileRepository FileRepository;
  private final FileMapper fileMapper;
  private final FileStorage FileStorage;

  @Transactional
  public FileDto create(FileCreateRequest request ) {
    String fileName = request.fileName();
    byte[] bytes = request.bytes();
    String contentType = request.contentType();
    File file = new File(
        fileName,
        contentType,
        (long) bytes.length
    );
    FileRepository.save(file);
    FileStorage.put(file.getId(), bytes);

    return fileMapper.toDto(file);
  }

  public FileDto find(Long fileId) {
    return FileRepository.findById(fileId)
        .map(fileMapper::toDto)
        .orElseThrow(() -> new NoSuchElementException(
            "File with id" + fileId + " not found"));
  }

  @Transactional
  public void delete(Long fileId ) {
    if (!FileRepository.existsById(fileId)) {
      throw new NoSuchElementException("File with id" + fileId + " not found");
    }
    FileRepository.deleteById(fileId);
    FileStorage.delete(fileId);
  }
}
