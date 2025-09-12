package com.codeit.HRBank.service;

import com.codeit.HRBank.domain.ChangeLogType;
import com.codeit.HRBank.domain.Change_log;
import com.codeit.HRBank.domain.Change_log_diff;
import com.codeit.HRBank.domain.Department;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.domain.File;
import com.codeit.HRBank.domain.EmploymentStatus;
import com.codeit.HRBank.dto.request.EmployeeRegistrationRequest;
import com.codeit.HRBank.dto.request.EmployeeUpdateRequest;
import com.codeit.HRBank.dto.request.FileCreateRequest;
import com.codeit.HRBank.dto.data.FileDto;
import com.codeit.HRBank.dto.response.CursorPageResponseDepartmentDto;
import com.codeit.HRBank.dto.response.CursorPageResponseEmployeeDto;
import com.codeit.HRBank.dto.response.EmployeeDetailsResponse;
import com.codeit.HRBank.dto.response.EmployeeResponse;
import com.codeit.HRBank.exception.DuplicateEmailException;
import com.codeit.HRBank.mapper.EmployeeMapper;
import com.codeit.HRBank.repository.ChangeLogDiffRepository;
import com.codeit.HRBank.repository.ChangeLogRepository;
import com.codeit.HRBank.repository.DepartmentRepository;
import com.codeit.HRBank.repository.EmployeeRepository;
import com.codeit.HRBank.repository.FileRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final FileService fileService;
  private final FileRepository fileRepository;
  private final ChangeLogService changeLogService;
  private final EmployeeMapper employeeMapper;

  //직원 등록
  @Transactional
  public Employee registerNewEmployee(EmployeeRegistrationRequest request,
      MultipartFile profileImage) {
    validateEmail(request.getEmail());
    Department department = departmentRepository.findByName(request.getDepartmentName())
        .orElseThrow(
            () -> new NoSuchElementException(
                "부서 정보를 찾을 수 없습니다. ID: " + request.getDepartmentName()));

    String employeeNumber = generateEmployeeNumber();
    File profileFile = saveProfileImage(profileImage);

    Employee newEmployee = Employee.builder()
        .name(request.getName())
        .email(request.getEmail())
        .employeeNumber(employeeNumber)
        .department(department)
        .position(request.getPosition())
        .hireDate(request.getHireDate())
        .status(EmploymentStatus.ACTIVE)
        .profileImage(profileFile)
        .build();
    Employee savedEmployee = employeeRepository.save(newEmployee);
    changeLogService.create(newEmployee);
    return savedEmployee;
  }

  @Transactional(readOnly = true)
  protected void validateEmail(String email) {
    employeeRepository.findByEmail(email).ifPresent(e -> {
      throw new IllegalArgumentException("이미 등록된 이메일입니다: " + email);
    });
  }


  private String generateEmployeeNumber() {
    String prefix = "EMP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    Long count = employeeRepository.countByEmployeeNumberStartingWith(prefix);
    return String.format("%s-%03d", prefix, count + 1);
  }

  private File saveProfileImage(MultipartFile profileImage) {
    if (profileImage == null || profileImage.isEmpty()) {
      return null;
    }

    try {
      FileCreateRequest fileCreateRequest = new FileCreateRequest(
          profileImage.getOriginalFilename(),
          profileImage.getContentType(),
          profileImage.getBytes()
      );

      FileDto fileDto = fileService.create(fileCreateRequest);

      return fileRepository.findById(fileDto.id())
          .orElseThrow(() -> new NoSuchElementException("저장된 파일을 찾을 수 없습니다. ID: " + fileDto.id()));

    } catch (IOException e) {
      return null;
    }
  }

  //직원 삭제
  @Transactional
  public void deleteEmployee(Long id, String ipAddress) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("직원을 찾을 수 없습니다. ID: " + id));
    changeLogService.delete(employee, ipAddress);

    if (employee.getProfileImage() != null) {
      fileService.delete(employee.getProfileImage().getId());
    }
    employeeRepository.delete(employee);
  }

  //직원 정보 수정
  public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest updateRequest,
      String ipAddress) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("직원을 찾을 수 없습니다. ID: " + id));

    Employee originalEmployee = Employee.builder()
        .id(employee.getId())
        .name(employee.getName())
        .email(employee.getEmail())
        .employeeNumber(employee.getEmployeeNumber())
        .department(employee.getDepartment())
        .position(employee.getPosition())
        .hireDate(employee.getHireDate())
        .status(employee.getStatus())
        .profileImage(employee.getProfileImage())
        .build();

    if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(employee.getEmail())) {
      if (employeeRepository.existsByEmail(updateRequest.getEmail())) {
        throw new DuplicateEmailException("이미 사용 중인 이메일입니다: " + updateRequest.getEmail());
      }
    }

    if (updateRequest.getName() != null) {
      employee.setName(updateRequest.getName());
    }
    if (updateRequest.getEmail() != null) {
      employee.setEmail(updateRequest.getEmail());
    }
    if (updateRequest.getDepartmentName() != null) {
      Department department = departmentRepository.findByName(updateRequest.getDepartmentName())
          .orElseThrow(() -> new NoSuchElementException(
              "부서를 찾을 수 없습니다. ID: " + updateRequest.getDepartmentName()));
      employee.setDepartment(department);
    }
    if (updateRequest.getPosition() != null) {
      employee.setPosition(updateRequest.getPosition());
    }
    if (updateRequest.getStatus() != null) {
      employee.setStatus(updateRequest.getStatus());
    }
    if (updateRequest.getHireDate() != null) {
      employee.setHireDate(updateRequest.getHireDate());
    }
    if (updateRequest.getProfileImageId() != null) {
      File profileImage = fileRepository.findById(updateRequest.getProfileImageId())
          .orElseThrow(() -> new EntityNotFoundException(
              "프로필 이미지를 찾을 수 없습니다. ID: " + updateRequest.getProfileImageId()));

      if (employee.getProfileImage() != null) {
        fileService.delete(employee.getProfileImage().getId());
      }
      employee.setProfileImage(profileImage);

    } else if (employee.getProfileImage() != null) {
      fileService.delete(employee.getProfileImage().getId());
      employee.setProfileImage(null);
    }

    Employee updatedEmployee = employeeRepository.save(employee);

    changeLogService.update(originalEmployee, updatedEmployee, ipAddress);

    return EmployeeResponse.from(updatedEmployee);
  }


  //직원 상세 정보 조회
  public EmployeeDetailsResponse getEmployeeDetailsById(Long id) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("직원을 찾을 수 없습니다. ID: " + id));

    return EmployeeDetailsResponse.from(employee);
  }

  public CursorPageResponseEmployeeDto findByCondition(
      String nameOrEmail,
      String employeeNumber,
      String departmentName,
      String position,
      LocalDateTime hireDateFrom,
      LocalDateTime hireDateTo,
      EmploymentStatus status,
      Long idAfter,        // 이전 페이지의 마지막 ID
      String cursor,       // 커서(선택)
      Integer size,
      String sortField,
      String sortDirection) {

//        Long idAfter = request.idAfter();
//        String cursor = request.cursor();
    size = (size != null && size > 0) ? size : 10;
    sortField = (sortField != null) ? sortField : "name";
    sortDirection = (sortDirection != null) ? sortDirection : "ASC";
    Sort.Direction direction = Sort.Direction.fromString(sortDirection);
    Sort sort = Sort.by(direction, sortField);

    Pageable pageable = PageRequest.of(0, size, sort);

    Slice<Employee> employeeSlice = employeeRepository.findByCondition(
        nameOrEmail, employeeNumber, departmentName, position, hireDateFrom, hireDateTo, status,
        idAfter, pageable
    );

    return employeeMapper.toDtoSlice(employeeSlice);

  }

  public Long countByCondition(EmploymentStatus status, LocalDateTime fromDate,
      LocalDateTime toDate) {
    return employeeRepository.countByCondition(status, fromDate, toDate);
  }

}