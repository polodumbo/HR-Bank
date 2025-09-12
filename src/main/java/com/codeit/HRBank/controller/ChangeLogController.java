package com.codeit.HRBank.controller;

import com.codeit.HRBank.dto.data.ChangeLogDto;
import com.codeit.HRBank.dto.data.DiffDto;
import com.codeit.HRBank.service.ChangeLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/change-logs")
@RequiredArgsConstructor
public class ChangeLogController {

  private final ChangeLogService changeLogService;

  @GetMapping("/{employeeNumber}")
  public ResponseEntity<ChangeLogDto> find(@PathVariable String employeeNumber) {
    ChangeLogDto created = changeLogService.find(employeeNumber);
    return ResponseEntity.status(HttpStatus.OK).body(created);
  }

  @GetMapping("/{id}/diffs")
  public ResponseEntity<List<DiffDto>> getDiffsByLogId(@PathVariable Long id) {
    List<DiffDto> diffs = changeLogService.findDiffsByLogId(id);
    return ResponseEntity.ok(diffs);
  }

}
