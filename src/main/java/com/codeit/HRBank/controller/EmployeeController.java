package com.codeit.HRBank.controller;

import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.dto.response.EmployeeResponse;
import com.codeit.HRBank.dto.request.EmployeeRegistrationRequest;
import com.codeit.HRBank.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
}
