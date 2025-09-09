package com.codeit.HRBank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "부서 관리", description = "부서 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/departments")
public class DepartmentController {

    /* private final DepartmentService departmentService;

    @Operation(summary = "부서 목록 조회")
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    @Operation(summary = "부서 등록")
    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@RequestBody DepartmentCreateRequest request) {
        DepartmentResponse res = departmentService.create(req);
        return ResponseEntity.created(URI.create("/api/departments/" + res.id()))).body.(res);
    }

    @Operation(summary = "부서 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> find(@PathVariable Long id) {
        DepartmentResponse res = departmentService.findById(id);
        return (res == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(res);
    }

    @Operation(summary = "부서 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

    }*/

}
