package com.codeit.HRBank.storage.local;

import com.codeit.HRBank.dto.data.FileDto;
import com.codeit.HRBank.storage.FileStorage;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hrbank.storage.type", havingValue = "local")
public class LocalFileStorage implements FileStorage {

  private final Path root;

  public LocalFileStorage(
      @Value(".hrbank/storage") Path root
  ) {
    this.root = root;
  }

  @PostConstruct
  public void init() {
    if (!Files.exists(root)) {
      try {
        Files.createDirectories(root);
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public Long put(Long fileId, byte[] bytes) {
    Path filePath = resolvePath(fileId);
    if (Files.exists(filePath)) {
      throw new IllegalArgumentException("File already exists!");
    }
    try (OutputStream outputStream = Files.newOutputStream(filePath)) {
      outputStream.write(bytes);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return fileId;
  }

  @Override
  public InputStream get(Long fileId) {
    Path filePath = resolvePath(fileId);
    if (Files.notExists(filePath)) {
      throw new NoSuchElementException("File with key " + fileId + " does not exist");
    }
    try {
      return Files.newInputStream(filePath);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private Path resolvePath(Long fileId) {
    return root.resolve(fileId.toString());
  }

  @Override
  public ResponseEntity<Resource> download(FileDto metaData) {
    InputStream inputStream = get(metaData.id());
    Resource resource = new InputStreamResource(inputStream);

    return ResponseEntity
        .status(HttpStatus.OK)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + metaData.fileName() + "\"")
        .header(HttpHeaders.CONTENT_TYPE, metaData.contentType())
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(metaData.size()))
        .body(resource);
  }

  @Override
  public void delete(Long fileId) {
    Path filePath = resolvePath(fileId);
    if (Files.notExists(filePath)) {
      throw new NoSuchElementException("File" + fileId + "not found");
    }
    try {
      Files.delete(filePath);          // 실제 파일 삭제
    } catch (IOException e) {
      throw new RuntimeException("파일 삭제 실패: " + fileId, e);
    }

  }


}
