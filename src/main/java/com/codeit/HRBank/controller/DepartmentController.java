package com.codeit.HRBank.controller;

import com.codeit.HRBank.dto.data.DepartmentDto;
import com.codeit.HRBank.dto.request.DepartmentCreateRequest;
import com.codeit.HRBank.dto.request.DepartmentUpdateRequest;
import com.codeit.HRBank.dto.response.CursorPageResponseBackupDto;
import com.codeit.HRBank.dto.response.CursorPageResponseDepartmentDto;
import com.codeit.HRBank.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "부서 관리", description = "부서 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/departments")
public class DepartmentController {

  private final DepartmentService departmentService;

  @Operation(summary = "부서 등록")
  @PostMapping
  public ResponseEntity<DepartmentDto> create(
      @RequestBody DepartmentCreateRequest request
  ) {
    DepartmentDto created = departmentService.create(request);
    return ResponseEntity.status(HttpStatus.OK).body(created);
  }

  @Operation(summary = "부서 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    boolean deleted = departmentService.delete(id);
    return (deleted) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
  }

  @Operation(summary = "부서 수정")
  @PatchMapping("/{id}")
  public ResponseEntity<DepartmentDto> update(@PathVariable Long id,
      @RequestBody DepartmentUpdateRequest request) {
    DepartmentDto res = departmentService.update(id, request);
    return (res == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(res);
  }

  @Operation(summary = "부서 상세 조회")
  @GetMapping("/{id}")
  public ResponseEntity<DepartmentDto> findByDepartmentId(@PathVariable("id") Long departmentid) {
    DepartmentDto departments = departmentService.find(departmentid);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(departments);
  }

  @Operation(summary = "부서 목록 조회")
  @GetMapping
  public ResponseEntity<CursorPageResponseDepartmentDto> find(
      @RequestParam(required = false) String nameOrDescription,
      @RequestParam(required = false) Long idAfter,        // 이전 페이지의 마지막 ID
      @RequestParam(required = false) String cursor,       // 커서(선택)
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(defaultValue = "establishedDate") String sortField,
      @RequestParam(defaultValue = "asc") String sortDirection
  ){
      CursorPageResponseDepartmentDto response = departmentService.findByCondition(nameOrDescription, idAfter, cursor, size, sortField, sortDirection);
      return ResponseEntity.
              status(HttpStatus.OK).body(response);
  }


}
