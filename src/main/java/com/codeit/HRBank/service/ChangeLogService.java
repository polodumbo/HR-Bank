package com.codeit.HRBank.service;

import com.codeit.HRBank.domain.ChangeLogType;
import com.codeit.HRBank.domain.Change_log;
import com.codeit.HRBank.dto.data.DiffDto;
import com.codeit.HRBank.repository.ChangeLogDiffRepository;
import java.util.List;
import java.util.NoSuchElementException;
import com.codeit.HRBank.domain.Change_log_diff;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.dto.data.ChangeLogDto;
import com.codeit.HRBank.mapper.ChangeLogMapper;
import com.codeit.HRBank.repository.ChangeLogRepository;
import com.codeit.HRBank.repository.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeLogService {

  private final ChangeLogRepository changeLogRepository;
  private final HttpServletRequest request;
  private final ChangeLogMapper changeLogMapper;
  private final ChangeLogDiffService changeLogDiffService;
  private final ChangeLogDiffRepository changeLogDiffRepository;

  public String getClientIpAddress() {
    String ipAddress = request.getHeader("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
  }

  public ChangeLogDto create(Employee newEmployee) {
    Change_log log = Change_log.builder()
        .type(ChangeLogType.CREATED)
        .employeeNumber(newEmployee.getEmployeeNumber())
        .memo("새 직원 등록")
        .ipAddress(getClientIpAddress())
        .at(LocalDateTime.now())
        .build();
    Change_log savedLog = changeLogRepository.save(log);
    changeLogDiffService.create(savedLog, newEmployee);
    return changeLogMapper.toDto(savedLog);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public ChangeLogDto delete(Employee newEmployee, String ipAddress) {
    Change_log log = Change_log.builder()
        .type(ChangeLogType.DELETED)
        .employeeNumber(newEmployee.getEmployeeNumber())
        .memo("직원 삭제")
        .ipAddress(ipAddress)
        .at(LocalDateTime.now())
        .build();
    Change_log savedLog = changeLogRepository.save(log);
    changeLogDiffService.delete(savedLog, newEmployee);
    return changeLogMapper.toDto(savedLog);
  }

  public ChangeLogDto update(Employee originalEmployee, Employee updatedEmployee,
      String ipAddress) {
    Change_log log = Change_log.builder()
        .type(ChangeLogType.UPDATED)
        .employeeNumber(originalEmployee.getEmployeeNumber())
        .memo("직원 정보 수정")
        .ipAddress(ipAddress)
        .at(LocalDateTime.now())
        .build();
    Change_log savedLog = changeLogRepository.save(log);
    changeLogDiffService.update(savedLog, originalEmployee, updatedEmployee);
    return changeLogMapper.toDto(savedLog);
  }


  public ChangeLogDto find(String employeeNumber) {
    return changeLogRepository.findByEmployeeNumber(employeeNumber)
        .map(changeLogMapper::toDto)
        .orElseThrow(
            () -> new NoSuchElementException(
                "ChangeLog with employeeNumber" + employeeNumber + "notfound"));
  }

  public List<DiffDto> findDiffsByLogId(Long logId) {
    //존재 확인: 없으면 404
    changeLogRepository.findById(logId)
        .orElseThrow(() -> new NoSuchElementException("로그 없음: " + logId));

    return changeLogDiffRepository.findAllByLog_Id(logId).stream()
        .map(d -> new DiffDto(d.getPropertyName(), d.getBeforeValue(), d.getAfterValue()))
        .toList();
  }

}
