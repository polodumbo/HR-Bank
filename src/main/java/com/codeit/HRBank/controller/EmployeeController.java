package com.codeit.HRBank.controller;

import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.dto.request.EmployeeUpdateRequest;
import com.codeit.HRBank.dto.request.FileCreateRequest;
import com.codeit.HRBank.dto.response.EmployeeDetailsResponse;
import com.codeit.HRBank.dto.response.EmployeeResponse;
import com.codeit.HRBank.dto.request.EmployeeRegistrationRequest;
import com.codeit.HRBank.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
      @RequestBody EmployeeUpdateRequest updateRequest,
      HttpServletRequest request
  ) {
    String ipAddress = request.getRemoteAddr();
    EmployeeResponse updatedEmployee = employeeService.updateEmployee(id, updateRequest, ipAddress);
    return ResponseEntity.ok(updatedEmployee);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EmployeeDetailsResponse> getEmployeeDetails(@PathVariable Long id) {
    EmployeeDetailsResponse response = employeeService.getEmployeeDetailsById(id);
    return ResponseEntity.ok(response);
  }
}
