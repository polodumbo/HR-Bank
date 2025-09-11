package com.codeit.HRBank.service;

import com.codeit.HRBank.domain.ChangeLogType;
import com.codeit.HRBank.domain.Change_log;
import com.codeit.HRBank.domain.Change_log_diff;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.dto.data.ChangeLogDto;
import com.codeit.HRBank.mapper.ChangeLogMapper;
import com.codeit.HRBank.repository.ChangeLogRepository;
import com.codeit.HRBank.repository.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeLogService {

  private final ChangeLogRepository changeLogRepository;
  private final HttpServletRequest request;
  private final ChangeLogMapper changeLogMapper;
  private final ChangeLogDiffService changeLogDiffService;

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
        .ip_address(getClientIpAddress())
        .at(LocalDateTime.now())
        .build();
    Change_log savedLog = changeLogRepository.save(log);
    changeLogDiffService.create(savedLog, newEmployee);
    return changeLogMapper.toDto(savedLog);
  }

}
