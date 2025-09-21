package com.codeit.HRBank.controller;

import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.domain.EmploymentStatus;
import com.codeit.HRBank.dto.data.EmployeeDistributionDto;
import com.codeit.HRBank.dto.data.EmployeeTrendDto;
import com.codeit.HRBank.dto.request.EmployeeRegistrationRequest;
import com.codeit.HRBank.dto.request.EmployeeUpdateRequest;
import com.codeit.HRBank.dto.response.CursorPageResponseEmployeeDto;
import com.codeit.HRBank.dto.response.EmployeeDetailsResponse;
import com.codeit.HRBank.dto.response.EmployeeResponse;
import com.codeit.HRBank.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeResponse> registerEmployee(
        @RequestPart("employee") @Valid EmployeeRegistrationRequest request,
        @RequestPart(value = "profile", required = false) MultipartFile profileImage) {
        Employee newEmployee = employeeService.registerNewEmployee(request, profileImage);
        EmployeeResponse response = EmployeeResponse.from(newEmployee);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") Long id,
        HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        employeeService.deleteEmployee(id, ipAddress);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
        @PathVariable Long id,
        @RequestPart("employee") EmployeeUpdateRequest updateRequest,
        @RequestPart(value = "profile", required = false) MultipartFile profileImage,
        HttpServletRequest request
    ) {
        String ipAddress = request.getRemoteAddr();
        EmployeeResponse updatedEmployee = employeeService.updateEmployee(id, updateRequest,
            ipAddress, profileImage);
        return ResponseEntity.ok(updatedEmployee);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDetailsResponse> getEmployeeDetails(@PathVariable Long id) {
        EmployeeDetailsResponse response = employeeService.getEmployeeDetailsById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseEmployeeDto> find(
        @RequestParam(required = false) String nameOrEmail,
        @RequestParam(required = false) String employeeNumber,
        @RequestParam(required = false) String departmentName,
        @RequestParam(required = false) String position,
        @RequestParam(required = false) LocalDate hireDateFrom,
        @RequestParam(required = false) LocalDate hireDateTo,
        @RequestParam(required = false) EmploymentStatus status,
        @RequestParam(required = false) Long idAfter,        // 이전 페이지의 마지막 ID
        @RequestParam(required = false) String cursor,       // 커서(선택)
        @RequestParam(defaultValue = "10") Integer size,
        @RequestParam(defaultValue = "name") String sortField,
        @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        CursorPageResponseEmployeeDto response = employeeService.findByCondition(
            nameOrEmail, employeeNumber, departmentName, position, hireDateFrom, hireDateTo, status,
            idAfter, cursor, size, sortField, sortDirection);
        return ResponseEntity.
            status(HttpStatus.OK).body(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count(
        @RequestParam(required = false) EmploymentStatus status,
        @RequestParam(required = false) LocalDate fromDate,
        @RequestParam(required = false) LocalDate toDate

    ) {
        Long response = employeeService.countByCondition(status, fromDate, toDate);
        return ResponseEntity.
            status(HttpStatus.OK).body(response);
    }

    @GetMapping("/stats/trend")
    public ResponseEntity<List<EmployeeTrendDto>> getTrend(
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to,
        @RequestParam(defaultValue = "month") String unit
    ) {

        List<EmployeeTrendDto> response = employeeService.getTrend(from, to, unit);
        return ResponseEntity.
            status(HttpStatus.OK).body(response);
    }

    @GetMapping("/stats/distribution")
    public ResponseEntity<List<EmployeeDistributionDto>> getDistribution(
        @RequestParam(defaultValue = "department") String groupBy,
        @RequestParam(defaultValue = "ACTIVE") EmploymentStatus status
    ) {
        List<EmployeeDistributionDto> response = employeeService.getDistribution(groupBy, status);
        return ResponseEntity.
            status(HttpStatus.OK).body(response);

    }
}
