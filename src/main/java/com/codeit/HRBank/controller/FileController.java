package com.codeit.HRBank.controller;

import com.codeit.HRBank.dto.data.FileDto;
import com.codeit.HRBank.service.FileService;
import com.codeit.HRBank.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final FileStorage fileStorage;

    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable("id") Long fileId) {
        FileDto fileDto = fileService.find(fileId);
        return fileStorage.download(fileDto);
    }

}
