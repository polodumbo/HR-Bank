package com.codeit.HRBank.storage;

import com.codeit.HRBank.dto.data.FileDto;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface FileStorage {

  Long put(Long fileId, byte[] bytes, String path);

  InputStream get(Long fileId, String path);

  ResponseEntity<Resource> download(FileDto metaData, String path);

  void delete(Long fileId, String path);

}
