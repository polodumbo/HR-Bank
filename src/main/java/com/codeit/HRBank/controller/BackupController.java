package com.codeit.HRBank.controller;

import com.codeit.HRBank.domain.BackupStatus;
import com.codeit.HRBank.domain.File;
import com.codeit.HRBank.dto.data.BackupDto;
import com.codeit.HRBank.dto.data.FileDto;
import com.codeit.HRBank.dto.request.BackupFindRequest;
import com.codeit.HRBank.dto.response.CursorPageResponseBackupDto;
import com.codeit.HRBank.repository.FileRepository;
import com.codeit.HRBank.service.BackupService;
import com.codeit.HRBank.storage.FileStorage;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backups")
public class BackupController {
    private final BackupService backupService;
    private final FileRepository fileRepository;
    private final FileStorage fileStorage;

    @PostMapping
    public ResponseEntity<BackupDto> create(HttpServletRequest request){
        BackupDto createdBackup = backupService.create(request.getRemoteAddr());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdBackup);
    }

    @GetMapping(path = "/{id}/download")
    public ResponseEntity<?> download(@PathVariable("id") Long id){
        File file = fileRepository.findById(id).get();
        FileDto dto = new FileDto(file.getId(), file.getFileName(), file.getSize(), file.getContentType());
        return fileStorage.download(dto);
    }
    /*
    - **{작업자}**, **{시작 시간}**, **{상태}**로 이력 목록을 조회할 수 있습니다.
    - **{작업자}**는 부분 일치 조건입니다.
    - **{시작 시간}**는 범위 조건입니다.
    - **{상태}**는 완전 일치 조건입니다.
    - 조회 조건이 여러 개인 경우 모든 조건을 만족한 결과로 조회합니다.*/
    //페이지네이션 (정렬조건)
    @GetMapping
    public ResponseEntity<CursorPageResponseBackupDto> findByConfidence(
            @RequestParam(required = false) String worker,
            @RequestParam(required = false) BackupStatus status,
            @RequestParam(required = false) LocalDate startedAtFrom,
            @RequestParam(required = false) LocalDate startedAtTo,
            @RequestParam(required = false) Long idAfter,        // 이전 페이지의 마지막 ID
            @RequestParam(required = false) String cursor,       // 커서(선택)
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "at") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection

    ){
        CursorPageResponseBackupDto response = backupService.findByCondition(worker, status, startedAtFrom, startedAtTo, idAfter, cursor, size, sortField, sortDirection);
        return ResponseEntity.
        status(HttpStatus.OK).body(response);
    }

    //지정된 상태의 가장 최근 백업 정보를 조회합니다.
    //상태를 지정하지 않으면 성공적으로 완료된(COMPLETED) 백업을 반환합니다.
    @GetMapping(path = "/latest")
    public ResponseEntity<BackupDto> findLatest( @RequestBody(required = false) BackupStatus status){
        BackupDto backupDto = backupService.findLatest(status);
        return ResponseEntity.
                status(HttpStatus.OK).body(backupDto);
    }

}
