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
import com.codeit.HRBank.repository.EmployeeQueryRepository;
import com.codeit.HRBank.repository.EmployeeRepository;
import com.codeit.HRBank.repository.FileRepository;
import com.querydsl.core.Tuple;
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
    private final EmployeeQueryRepository employeeQueryRepository;
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
        String prefix = "EMP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-%";
        Optional<String> lastEmployeeNumber = employeeRepository.findLastEmployeeNumberStartingWith(prefix);

        String newEmployeeNumber;
        if (lastEmployeeNumber.isPresent()) {
            // 가장 최근 번호가 있다면, 그 번호에 1을 더해 새 번호 생성
            // (이 로직은 동시성 문제를 고려해야 합니다.)
            newEmployeeNumber = generateNextNumber(lastEmployeeNumber.get());
        } else {
            // 없다면 첫 번째 번호 생성
            newEmployeeNumber = "EMP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-001";
        }
        return newEmployeeNumber;
    }
    public String generateNextNumber(String lastNumber) {
        // 1. "EMP-2025-09-005"에서 "005" 부분을 추출
        String prefix = lastNumber.substring(0, lastNumber.lastIndexOf('-') + 1);
        String numberPart = lastNumber.substring(lastNumber.lastIndexOf('-') + 1);

        // 2. 숫자를 Long으로 변환하고 1을 더함
        long number = Long.parseLong(numberPart) + 1;

        // 3. 다시 "006"과 같이 세 자리 숫자로 포맷팅
        String nextNumber = String.format("%03d", number);

        // 4. 접두사와 합쳐서 최종 사원 번호 생성
        return prefix + nextNumber;
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

        List<Tuple> queryResult = employeeQueryRepository.getEmployeeTrend(finalFrom, finalTo, finalUnit);
        List<EmployeeTrendDto> trendList = new ArrayList<>();
        Long previousTotalCount = 0L; // 이전 시점의 누적 직원 수
        Long currentTotalCount = 0L; // 현재 시점의 누적 직원 수

        for (Tuple tuple : queryResult) {
            LocalDate date = tuple.get(0, LocalDate.class);
            Long newHiresInPeriod = tuple.get(1, Long.class); // 해당 기간에 추가된 직원 수

            Long change = 0L;
            double changeRate = 0.0;

            // 현재 시점의 누적 직원 수를 계산
            currentTotalCount += newHiresInPeriod;

            // 첫 번째 데이터가 아닐 때만 증감 및 증감률 계산
            if (previousTotalCount > 0) {
                change = currentTotalCount - previousTotalCount;
                changeRate = (double) change / previousTotalCount * 100.0;
            }

            // 첫 번째 데이터일 때도 change는 newHiresInPeriod와 동일
            if(previousTotalCount == 0) {
                change = newHiresInPeriod;
                if(change > 0) changeRate = 100.0;
            }

            trendList.add(
                    new EmployeeTrendDto(
                            date,
                            currentTotalCount, // DTO의 count는 누적 직원 수
                            change,
                            changeRate
                    )
            );

            previousTotalCount = currentTotalCount;
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