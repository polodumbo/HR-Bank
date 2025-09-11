package com.codeit.HRBank.service;


import com.codeit.HRBank.domain.Backup;
import com.codeit.HRBank.domain.BackupStatus;
import com.codeit.HRBank.domain.Change_log;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.domain.File;
import com.codeit.HRBank.dto.data.BackupDto;
import com.codeit.HRBank.dto.request.BackupFindRequest;
import com.codeit.HRBank.dto.response.CursorPageResponseBackupDto;
import com.codeit.HRBank.mapper.BackupMapper;
import com.codeit.HRBank.repository.BackupRepository;
import com.codeit.HRBank.repository.ChangeLogRepository;
import com.codeit.HRBank.repository.EmployeeRepository;
import com.codeit.HRBank.repository.FileRepository;
import com.codeit.HRBank.storage.FileStorage;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.text.html.parser.Entity;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BackupService {

    private final BackupRepository backupRepository;
    private final FileRepository fileRepository;
    private final BackupMapper backupMapper;
    private final EmployeeRepository employeeRepository;

    private final FileStorage fileStorage;

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private final ChangeLogRepository changeLogRepository;

    @Transactional
    public BackupDto create(String worker_ip) {
        log.info("1111111111");
        Backup backup = Backup.builder()
                .worker(worker_ip)
                .status(BackupStatus.IN_PROGRESS)
                .build();
        backup = backupRepository.save(backup);

        try {
            log.info("22222222222");


            List<Employee> employees = employeeRepository.findAll();

            if (employees.isEmpty()) {
                log.warn("백업할 직원 데이터가 없습니다.");
                backup.setStatus(BackupStatus.SKIPPED);
                backup.setEndedAt(LocalDateTime.now());
                backup = backupRepository.save(backup);
                log.info("3333333333");
                return backupMapper.toDto(backup);

            }

            if(!checkBackupProcess()){
                backup.setStatus(BackupStatus.SKIPPED);
                backup.setEndedAt(LocalDateTime.now());
                backup = backupRepository.save(backup);
                backup = backupRepository.save(backup);
                log.info("44444444");

                return backupMapper.toDto(backup);
            }else{
                log.info("555555");

            // CSV 파일 생성 및 바이트 배열 추출
            byte[] csvBytes;
            String fileName = "employees-backup-" + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".csv";
            String filePath = "backups/" + fileName;

            // 메모리에 CSV 데이터를 작성하는 로직으로 변경
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(bos,
                            StandardCharsets.UTF_8)) {

                writer.append(
                        "id,name,email,employee_number,department_id,position,hire_date,status\n");

                for (Employee employee : employees) {
                    writer.append(String.join(",",
                            String.valueOf(employee.getId()),
                            employee.getName(),
                            employee.getEmail(),
                            employee.getEmployeeNumber(),
                            employee.getDepartment() != null ? String.valueOf(
                                    employee.getDepartment().getId()) : "",
                            employee.getPosition(),
                            employee.getHireDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            employee.getStatus().name()
                    ));
                    writer.append("\n");
                }
                writer.flush();

                csvBytes = bos.toByteArray();

                // 파일 정보 엔티티로 저장
//                File file = File.builder()
//                        .fileName(fileName)
//                        .contentType("text/csv")
//                        .size((long) csvBytes.length)
//                        .build();
                File file = new File(fileName, "text/csv", (long) csvBytes.length);
                file = fileRepository.save(file);

                // 백업 이력 업데이트
                backup.setFile(file);
                backup.setStatus(BackupStatus.COMPLETED);
                backup.setEndedAt(LocalDateTime.now());
                backupRepository.save(backup);
                fileStorage.put(file.getId(), csvBytes);

                log.info("자동 백업 작업이 완료되었습니다. 파일 정보 ID: {}", file.getId());

            } catch (IOException e) {
                log.error("CSV 파일 생성 중 오류가 발생했습니다.", e);
                backup.setStatus(BackupStatus.FAILED);
                backup.setEndedAt(LocalDateTime.now());
                backup.setFile(null);
                backupRepository.save(backup);
                return backupMapper.toDto(backup);
            }
}

        } catch (Exception e) {
            log.error("자동 백업 작업 중 예상치 못한 오류가 발생했습니다.", e);
            backup.setStatus(BackupStatus.FAILED);
            backup.setEndedAt(LocalDateTime.now());
            backup.setFile(null);
            backupRepository.save(backup);
        }

        return backupMapper.toDto(backup);

    }

    @Transactional
    public CursorPageResponseBackupDto findByCondition(BackupFindRequest request) {
        String worker = request.worker();
        LocalDateTime startedAtFrom = request.startedAtFrom();
        LocalDateTime startedAtTo = request.startedAtTo();
        BackupStatus status = request.status();

        Long idAfter = request.idAfter();
        String cursor = request.cursor();
        int size = (request.size() != null && request.size() > 0) ? request.size() : 10;
        String sortField = (request.sortField() != null) ? request.sortField() : "startedAt";
        String sortDirection = (request.sortDirection() != null) ? request.sortDirection() : "DESC";

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Sort sort = Sort.by(direction, sortField);

        Pageable pageable = PageRequest.of(0, size, sort);

        Slice<Backup> backupSlice = backupRepository.findByCondition(
                worker, startedAtFrom, startedAtTo, status, idAfter, pageable
        );

        return backupMapper.toDtoSlice(backupSlice);

    }

    @Transactional
    public BackupDto findLatest(BackupStatus status) {
        Backup backup = backupRepository.findLatest(status);
        return backupMapper.toDto(backup);
    }

    //    1시간마다 자동백업
    @Scheduled(cron = "0 0 0/1 * * *")
//    @Scheduled(fixedRate = 5000) //테스트용 5초마다 백업
    public void runBackupProcess() {
        log.info("자동 백업 배치 작업을 시작합니다.");
        create("worker");
    }

    Boolean checkBackupProcess() {

        // 1. 마지막으로 'COMPLETED'된 백업의 시작 시간을 가져옴
        LocalDateTime lastBackupTime = null;
        Backup lastCompletedBackup = backupRepository.findLatest(BackupStatus.COMPLETED);


        if (lastCompletedBackup == null) {
            return true;
        }
        lastBackupTime = lastCompletedBackup.getStartedAt();
        log.info("마지막백업시간: {}", lastBackupTime);


        // 2. 가장 최근 직원 정보 수정 시간 가져옴
        Optional<Change_log> latestChangeLog = changeLogRepository.findFirstByOrderByAtDesc();

        // 3. 마지막 백업 시간과 최근 변경 이력 시간 비교
        if (latestChangeLog.isPresent()) {
            LocalDateTime latestChangeTime = latestChangeLog.get().getAt();
            log.info("마지막 로그 시간: {}", latestChangeTime);

            if(latestChangeTime.isBefore(lastBackupTime)) {
                return false;
            }
        }
        return true;
    }


}
