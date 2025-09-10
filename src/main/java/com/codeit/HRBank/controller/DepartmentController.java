package com.codeit.HRBank.controller;

import com.codeit.HRBank.domain.Department;
import com.codeit.HRBank.dto.response.PageResponse;
import com.codeit.HRBank.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "부서 관리", description = "부서 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/departments")
public class DepartmentController {

  private final DepartmentService departmentService;

  @Operation(summary = "부서 상세 조회")
  @GetMapping("/{id}")
  public ResponseEntity<DepartmentDto> findByDepartmentId(@PathVariable Long departmentid) {
    DepartmentDto departments = departmentService.find(departmentid);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(departments);
  }

  @GetMapping
  public ResponseEntity<PageResponse<DepartmentDto>> find


}
