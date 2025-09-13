package com.codeit.HRBank.service;

import com.codeit.HRBank.domain.Department;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.domain.File;
import com.codeit.HRBank.domain.EmploymentStatus;
import com.codeit.HRBank.dto.data.EmployeeDistributionDto;
import com.codeit.HRBank.dto.data.EmployeeTrendDto;
import com.codeit.HRBank.dto.request.EmployeeRegistrationRequest;
import com.codeit.HRBank.dto.request.EmployeeUpdateRequest;
import com.codeit.HRBank.dto.request.FileCreateRequest;
import com.codeit.HRBank.dto.data.FileDto;
import com.codeit.HRBank.dto.response.CursorPageResponseEmployeeDto;
import com.codeit.HRBank.dto.response.EmployeeDetailsResponse;
import com.codeit.HRBank.dto.response.EmployeeResponse;
import com.codeit.HRBank.exception.DuplicateEmailException;
import com.codeit.HRBank.mapper.EmployeeMapper;
import com.codeit.HRBank.repository.DepartmentRepository;
import com.codeit.HRBank.repository.EmployeeRepository;
import com.codeit.HRBank.repository.FileRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
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
@Slf4j
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
        validateEmail(request.email());

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NoSuchElementException(
                        "부서 정보를 찾을 수 없습니다. ID: " + request.departmentId()));

        String employeeNumber = generateEmployeeNumber();
        File profileFile = saveProfileImage(profileImage);

        Employee newEmployee = Employee.builder()
                .name(request.name())
                .email(request.email())
                .employeeNumber(employeeNumber)
                .department(department)
                .position(request.position())
                .hireDate(request.hireDate())
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
                    .orElseThrow(() -> new NoSuchElementException(
                            "저장된 파일을 찾을 수 없습니다. ID: " + fileDto.id()));

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
            String ipAddress, MultipartFile profileImage) {

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

        if (updateRequest.getEmail() != null && !updateRequest.getEmail()
                .equals(employee.getEmail())) {
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
                    .orElseThrow(() -> new NoSuchElementException(
                            "부서를 찾을 수 없습니다. ID: " + updateRequest.getDepartmentId()));
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
        if (profileImage != null && !profileImage.isEmpty()) {
            if (employee.getProfileImage() != null) {
                fileService.delete(employee.getProfileImage().getId());
            }
            File newProfileFile = saveProfileImage(profileImage);
            employee.setProfileImage(newProfileFile);
        } else {
            if (updateRequest.getProfileImageId() == null) {
                if (employee.getProfileImage() != null) {
                    fileService.delete(employee.getProfileImage().getId());
                }
                employee.setProfileImage(null);
            } else {
                File existingImage = fileRepository.findById(updateRequest.getProfileImageId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "포로필 이미지를 찾을 수 없습니다. ID: " + updateRequest.getProfileImageId()));
                employee.setProfileImage(existingImage);
            }
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
            LocalDate hireDateFrom,
            LocalDate hireDateTo,
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
                nameOrEmail, employeeNumber, departmentName, position, hireDateFrom, hireDateTo,
                status,
                idAfter, pageable
        );

        return employeeMapper.toDtoSlice(employeeSlice);

    }

    public Long countByCondition(EmploymentStatus status, LocalDate fromDate,
            LocalDate toDate) {
        return employeeRepository.countByCondition(status, fromDate, toDate);
    }

    public List<EmployeeTrendDto> getTrend(
    LocalDate from,
    LocalDate to,
    String unit
    ) {
        String finalUnit = Optional.ofNullable(unit).orElse("month").toLowerCase();
        ChronoUnit chronoUnit = getChronoUnit(finalUnit);

        LocalDate finalFrom = Optional.ofNullable(from)
                .orElse(LocalDate.now().minus(12, chronoUnit));

        LocalDate finalTo = Optional.ofNullable(to)
                .orElse(LocalDate.now());

        List<Object[]> queryResult;
        switch (finalUnit) {
            case "day":
                queryResult = employeeRepository.getTrendByDay(finalFrom, finalTo);
                break;
            case "week":
                queryResult = employeeRepository.getTrendByWeek(finalFrom, finalTo);
                break;
            case "month":
                queryResult = employeeRepository.getTrendByMonth(finalFrom, finalTo);
                break;
            case "quarter":
                queryResult = employeeRepository.getTrendByQuarter(finalFrom, finalTo);
                break;
            case "year":
                queryResult = employeeRepository.getTrendByYear(finalFrom, finalTo);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 시간 단위입니다: " + unit);
        }

        List<EmployeeTrendDto> trendList = new ArrayList<>();
        Long previousCount = null;

        for (Object[] row : queryResult) {
            log.info("로그로그: {}",row[0].toString());
            String dateString =  (String) row[0];
            LocalDate date = LocalDate.parse(dateString.substring(0, 10));
            Long currentCount = ((Number) row[1]).longValue();
            Long change = 0L;
            double changeRate = 0.0;

            if (previousCount != null) {
                change = currentCount - previousCount;
                if (previousCount > 0) {
                    changeRate = (double) change / previousCount * 100.0;
                }
            }

            trendList.add(new EmployeeTrendDto(date, currentCount, change, changeRate));
            previousCount = currentCount;
        }

        return trendList;
    }

    private ChronoUnit getChronoUnit(String unit) {
        switch (unit) {
            case "day": return ChronoUnit.DAYS;
            case "week": return ChronoUnit.WEEKS;
            case "month": return ChronoUnit.MONTHS;
            case "quarter": return ChronoUnit.MONTHS; // 분기는 월 단위로 계산
            case "year": return ChronoUnit.YEARS;
            default: return ChronoUnit.MONTHS;
        }
    }

    public List<EmployeeDistributionDto> getDistribution(String groupBy, EmploymentStatus status) {
        List<Object[]> queryResult;

        if ("department".equalsIgnoreCase(groupBy)) {
            queryResult = employeeRepository.getDistributionByDepartment(status);
        } else {
            // 기본값은 'position'으로 설정
            queryResult = employeeRepository.getDistributionByPosition(status);
        }

        // 쿼리 결과를 DTO로 변환
        return queryResult.stream()
                .map(row -> new EmployeeDistributionDto(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        (Double) row[2]
                ))
                .collect(Collectors.toList());


    }

}