package com.codeit.HRBank.service;


import com.codeit.HRBank.domain.Backup;
import com.codeit.HRBank.domain.BackupStatus;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.domain.File;
import com.codeit.HRBank.dto.data.BackupDto;
import com.codeit.HRBank.dto.request.BackupFindRequest;
import com.codeit.HRBank.mapper.BackupMapper;
import com.codeit.HRBank.repository.BackupRepository;
import com.codeit.HRBank.repository.EmployeeRepository;
import com.codeit.HRBank.repository.FileRepository;
import com.codeit.HRBank.storage.FileStorage;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Transactional
    public BackupDto create(String worker_ip) {

        Backup backup = Backup.builder()
                .worker(worker_ip)
                .status(BackupStatus.IN_PROGRESS)
                .build();
        backup = backupRepository.save(backup);

        try {
            List<Employee> employees = employeeRepository.findAll();

            if (employees.isEmpty()) {
                log.warn("백업할 직원 데이터가 없습니다.");
                backup.setStatus(BackupStatus.SKIPPED);
                backup.setEndedAt(LocalDateTime.now());
                backup = backupRepository.save(backup);
                return backupMapper.toDto(backup);    //?
            }

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
                File file = new File(fileName, "text/csv", (long)csvBytes.length);
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
    public List<BackupDto> findByCondition(BackupFindRequest request) {
        String worker = request.worker();
        LocalDateTime startedAtFrom = request.startedAtFrom();
        LocalDateTime startedAtTo = request.startedAtTo();
        BackupStatus status = request.status();

        List<Backup> backupList = backupRepository.findByCondition(
                worker, startedAtFrom, startedAtTo, status
        );
        return backupList.stream()
                .map(backupMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BackupDto findLatest(BackupStatus status){
        Backup backup = backupRepository.findLatest(status);
        return backupMapper.toDto(backup);
    }

// 파일작업완료시 가능
//    1시간마다 자동백업
//    @Scheduled(cron = "0 0 0/1 * * *")
//    public void runBackupProcess() {
//        log.info("자동 백업 배치 작업을 시작합니다.");
//
//        // Create a new BackupHistory entry
//        Backup backup = new Backup();
//        backup.setWorker("system");
//        backup.setStartedAt(LocalDateTime.now());
//        backup.setStatus(BackupStatus.IN_PROGRESS);
////        backup.setFile("backup-" + LocalDateTime.now() + ".zip");
//        backup.setEndedAt(null);
//
//        try {
//            // Simulate backup process (e.g., calling a shell script or a service)
//            // A simple sleep for demonstration purposes
//            Thread.sleep(5000);
//
//            // Update status to COMPLETED and set end time
//            backup.setEndedAt(LocalDateTime.now());
//            backup.setStatus(BackupStatus.COMPLETED);
//            log.info("자동 백업 작업이 완료되었습니다.");
//
//        } catch (Exception e) {
//            // Update status to FAILED and set end time
//            backup.setEndedAt(LocalDateTime.now());
//            backup.setStatus(BackupStatus.FAILED);
//            log.error("자동 백업 작업 중 오류가 발생했습니다.", e);
//        } finally {
//            // Save the backup history to the database regardless of success or failure
//            backupRepository.save(backup);
//            log.info("백업 이력이 데이터베이스에 저장되었습니다. 이력 ID: {}", backup.getId());
//        }
//    }


}
