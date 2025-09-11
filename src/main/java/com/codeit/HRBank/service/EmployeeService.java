package com.codeit.HRBank.service;

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
    private final ChangeLogRepository changeLogRepository;
    private final ChangeLogDiffRepository changeLogDiffRepository;
    private final EmployeeMapper employeeMapper;

    //직원 등록
    public Employee registerNewEmployee(EmployeeRegistrationRequest request, MultipartFile profileImage) {
        validateEmail(request.getEmail());
        Department department = findDepartmentById(request.getDepartmentId());
        String employeeNumber = generateEmployeeNumber();
        File profileFile = saveProfileImage(profileImage);

        Employee newEmployee = Employee.builder()
            .name(request.getName())
            .email(request.getEmail())
            .employeeNumber(employeeNumber)
            .department(department)
            .position(request.getPosition())
            .hireDate(request.getHireDate().atStartOfDay())
            .status(EmploymentStatus.ACTIVE)
            .profileImage(profileFile)
            .build();

        return employeeRepository.save(newEmployee);
    }

    @Transactional(readOnly = true)
    protected void validateEmail(String email) {
        employeeRepository.findByEmail(email).ifPresent(e -> {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + email);
        });
    }

    @Transactional(readOnly = true)
    protected Department findDepartmentById(Long departmentId) {
        return departmentRepository.findById(departmentId)
            .orElseThrow(() -> new NoSuchElementException("부서 정보를 찾을 수 없습니다. ID: " + departmentId));
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
    public void deleteEmployee(Long id, String ipAddress) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("직원을 찾을 수 없습니다. ID: " + id));

        Change_log deletionLog = Change_log.builder()
            .type("DELETED")
            .employee(employee)
            .memo("직원 정보 물리적 삭제")
            .ip_address(ipAddress)
            .at(LocalDateTime.now())
            .build();

        changeLogRepository.save(deletionLog);

        if (employee.getProfileImage() != null) {
            fileService.delete(employee.getProfileImage().getId());
        }

        employeeRepository.delete(employee);
    }

    //직원 정보 수정
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest updateRequest, String ipAddress) {
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
        if (updateRequest.getDepartmentId() != null) {
            Department department = departmentRepository.findById(updateRequest.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("부서를 찾을 수 없습니다. ID: " + updateRequest.getDepartmentId()));
            employee.setDepartment(department);
        }
        if (updateRequest.getPosition() != null) {
            employee.setPosition(updateRequest.getPosition());
        }
        if (updateRequest.getHireDate() != null) {
            employee.setHireDate(updateRequest.getHireDate().atStartOfDay());
        }
        if (updateRequest.getProfileImageId() != null) {
            File profileImage = fileRepository.findById(updateRequest.getProfileImageId())
                .orElseThrow(()-> new EntityNotFoundException("프로필 이미지를 찾을 수 없습니다. ID: " + updateRequest.getProfileImageId()));

            if (employee.getProfileImage() != null) {
                fileService.delete(employee.getProfileImage().getId());
            }
            employee.setProfileImage(profileImage);

        } else if (employee.getProfileImage() != null) {
            fileService.delete(employee.getProfileImage().getId());
            employee.setProfileImage(null);
        }

        Employee updatedEmployee = employeeRepository.save(employee);

        //변경 이력 로깅
        logChanges(originalEmployee, updatedEmployee, ipAddress, "직원 정보 수정");

        return EmployeeResponse.from(updatedEmployee);
    }

    private void logChanges(Employee original, Employee updated, String ipAddress, String memo) {
        Change_log changeLog = Change_log.builder()
            .type("UPDATED")
            .employee(updated)
            .memo(memo)
            .ip_address(ipAddress)
            .at(LocalDateTime.now())
            .build();
        changeLogRepository.save(changeLog);

        if (!Objects.equals(original.getName(), updated.getName())) {
            changeLogDiffRepository.save(createChangeLogDiff(changeLog, "name", original.getName(), updated.getName()));
        }
        if (!Objects.equals(original.getEmail(), updated.getEmail())) {
            changeLogDiffRepository.save(createChangeLogDiff(changeLog, "email", original.getEmail(), updated.getEmail()));
        }
        if (!Objects.equals(original.getDepartment(), updated.getDepartment())) {
            changeLogDiffRepository.save(createChangeLogDiff(changeLog, "department",
                original.getDepartment().getName(), updated.getDepartment().getName()));
        }
        if (!Objects.equals(original.getPosition(), updated.getPosition())) {
            changeLogDiffRepository.save(createChangeLogDiff(changeLog, "position", original.getPosition(), updated.getPosition()));
        }
        if (!Objects.equals(original.getHireDate(), updated.getHireDate())) {
            changeLogDiffRepository.save(createChangeLogDiff(changeLog, "hire_date", original.getHireDate().toString(), updated.getHireDate().toString()));
        }
        if (!Objects.equals(original.getProfileImage(), updated.getProfileImage())) {
            String beforeImageId = (original.getProfileImage() != null) ? original.getProfileImage().getId().toString() : "null";
            String afterImageId = (updated.getProfileImage() != null) ? updated.getProfileImage().getId().toString() : "null";
            changeLogDiffRepository.save(createChangeLogDiff(changeLog, "profile_image", beforeImageId, afterImageId));
        }
    }

    private Change_log_diff createChangeLogDiff(Change_log log, String propertyName, String beforeValue, String afterValue) {
        return Change_log_diff.builder()
            .log(log)
            .property_name(propertyName)
            .beforeValue(beforeValue)
            .afterValue(afterValue)
            .build();
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
                nameOrEmail, employeeNumber, departmentName, position, hireDateFrom, hireDateTo, status, idAfter, pageable
        );

        return employeeMapper.toDtoSlice(employeeSlice);

    }

    public Long countByCondition(EmploymentStatus status, LocalDateTime fromDate, LocalDateTime toDate) {
        return employeeRepository.countByCondition(status, fromDate, toDate);
    }

}