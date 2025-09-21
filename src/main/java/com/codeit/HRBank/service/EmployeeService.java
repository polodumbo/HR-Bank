package com.codeit.HRBank.service;

import com.codeit.HRBank.domain.Department;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.domain.EmploymentStatus;
import com.codeit.HRBank.domain.File;
import com.codeit.HRBank.dto.data.EmployeeDistributionDto;
import com.codeit.HRBank.dto.data.EmployeeTrendDto;
import com.codeit.HRBank.dto.data.FileDto;
import com.codeit.HRBank.dto.request.EmployeeRegistrationRequest;
import com.codeit.HRBank.dto.request.EmployeeUpdateRequest;
import com.codeit.HRBank.dto.request.FileCreateRequest;
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
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
        String prefix =
            "EMP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-%";
        Optional<String> lastEmployeeNumber = employeeRepository.findLastEmployeeNumberStartingWith(
            prefix);

        String newEmployeeNumber;
        if (lastEmployeeNumber.isPresent()) {
            // 가장 최근 번호가 있다면, 그 번호에 1을 더해 새 번호 생성
            // (이 로직은 동시성 문제를 고려해야 합니다.)
            newEmployeeNumber = generateNextNumber(lastEmployeeNumber.get());
        } else {
            // 없다면 첫 번째 번호 생성
            newEmployeeNumber =
                "EMP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-001";
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
        LocalDate finalTo = Optional.ofNullable(to).orElse(LocalDate.now());

        // 1. 기간 시작 전 총 직원 수 조회
        Long totalCountAtStart = employeeQueryRepository.getEmployeeCountBefore(finalFrom);

        // 1. 입사자 수 조회 및 맵으로 변환
        List<Tuple> hiredResult = employeeQueryRepository.getHiredTrend(finalFrom, finalTo,
            finalUnit);
        Map<LocalDate, Long> hiredMap = hiredResult.stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(0, LocalDate.class),
                tuple -> tuple.get(1, Long.class)
            ));
//        log.info("로그로그hiredResult: {}",hiredResult.get(0).toString());
//
//        log.info("final from to {}, {}", finalFrom, finalTo);
        // 2. 퇴사자 수 조회 및 맵으로 변환
        List<Tuple> resignedResult = employeeQueryRepository.getResignedTrend(finalFrom, finalTo,
            finalUnit);
        Map<LocalDate, Long> resignedMap = resignedResult.stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(0, LocalDateTime.class).toLocalDate(),
                tuple -> tuple.get(1, Long.class)
            ));
//         리스트가 비어있지 않을 때만 로그를 출력하도록 조건 추가
        if (!resignedMap.isEmpty()) {
//            log.info("로그로그hiredResult: {}",hiredResult.get(0).toString());
//            log.info("로그로그resignedResult: {}", resignedResult.get(0).toString());
            log.info("resignedMap: {}", resignedMap);
            log.info("final from to {}, {}", finalFrom, finalTo);
        } else {
            log.info("로그로그:데이터 없음");
        }

        List<EmployeeTrendDto> trendList = new ArrayList<>();
//        Long previousTotalCount = 0L;
        Long previousTotalCount = totalCountAtStart; // <-- 초기값을 기간 시작 전 총 직원 수로 설정

        // 루프의 시작 날짜를 finalFrom의 첫째 날로 설정하고, 1단위씩 증가
        LocalDate currentDate = getTruncatedDate(finalFrom, finalUnit);

        for (; !currentDate.isAfter(finalTo); currentDate = currentDate.plus(1, chronoUnit)) {
            Long hiredCount = hiredMap.getOrDefault(currentDate, 0L);
            Long resignedCount = resignedMap.getOrDefault(currentDate, 0L);

            Long totalCount = previousTotalCount + hiredCount - resignedCount;
            Long change = hiredCount - resignedCount;
            double changeRate =
                previousTotalCount > 0 ? (double) change / previousTotalCount * 100.0 : 0.0;

            trendList.add(new EmployeeTrendDto(currentDate, totalCount, change, changeRate));
            previousTotalCount = totalCount;
        }

        return trendList;

    }

    // 날짜 단위를 기준으로 날짜를 자르는 헬퍼 메서드
    private LocalDate getTruncatedDate(LocalDate date, String unit) {
        switch (unit.toLowerCase()) {
            case "week":
            case "day":
                return date; // week와 day는 그대로 사용
            case "month":
            case "quarter":
                return date.withDayOfMonth(1);
            case "year":
                return date.withDayOfYear(1);
            default:
                return date;
        }
    }

    private ChronoUnit getChronoUnit(String unit) {
        switch (unit) {
            case "day":
                return ChronoUnit.DAYS;
            case "week":
                return ChronoUnit.WEEKS;
            case "month":
                return ChronoUnit.MONTHS;
            case "quarter":
                return ChronoUnit.MONTHS; // 분기는 월 단위로 계산
            case "year":
                return ChronoUnit.YEARS;
            default:
                return ChronoUnit.MONTHS;
        }
    }

    public List<EmployeeDistributionDto> getDistribution(String groupBy, EmploymentStatus status) {
        String finalGroupBy = Optional.ofNullable(groupBy).orElse("department");
        EmploymentStatus finalStatus = Optional.ofNullable(status).orElse(EmploymentStatus.ACTIVE);

        // 1. 전체 직원 수를 별도의 쿼리로 조회
        Long totalEmployees = employeeQueryRepository.countByStatus(finalStatus);

        // 2. 그룹별 직원 수를 튜플로 조회
        List<Tuple> distributionTuples = employeeQueryRepository.getDistributionTuple(finalGroupBy,
            finalStatus);

        // 3. 튜플을 DTO로 변환하며 퍼센티지 계산
        return distributionTuples.stream()
            .map(tuple -> {
                String groupKey = tuple.get(0, String.class);
                Long count = tuple.get(1, Long.class);
                double percentage = (double) count / totalEmployees * 100.0;

                return new EmployeeDistributionDto(groupKey, count, percentage);
            })
            .collect(Collectors.toList());
    }


}