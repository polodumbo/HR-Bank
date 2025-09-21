package com.codeit.HRBank.storage;

import com.codeit.HRBank.dto.data.FileDto;
import java.io.InputStream;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface FileStorage {

    Long put(Long fileId, byte[] bytes);

    InputStream get(Long fileId);

    ResponseEntity<Resource> download(FileDto metaData);

    void delete(Long fileId);

}
