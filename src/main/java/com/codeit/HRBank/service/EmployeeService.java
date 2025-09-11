package com.codeit.HRBank.service;

import com.codeit.HRBank.domain.Change_log;
import com.codeit.HRBank.domain.Department;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.domain.File;
import com.codeit.HRBank.domain.EmploymentStatus;
import com.codeit.HRBank.dto.request.EmployeeRegistrationRequest;
import com.codeit.HRBank.dto.request.FileCreateRequest;
import com.codeit.HRBank.dto.data.FileDto;
import com.codeit.HRBank.repository.ChangeLogRepository;
import com.codeit.HRBank.repository.DepartmentRepository;
import com.codeit.HRBank.repository.EmployeeRepository;
import com.codeit.HRBank.repository.FileRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            .at(Instant.now())
            .build();

        changeLogRepository.save(deletionLog);

        if (employee.getProfileImage() != null) {
            fileService.delete(employee.getProfileImage().getId());
        }

        employeeRepository.delete(employee);
    }
}