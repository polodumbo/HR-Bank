package com.codeit.HRBank.controller;

import com.codeit.HRBank.domain.ChangeLogType;
import com.codeit.HRBank.dto.data.ChangeLogDto;
import com.codeit.HRBank.dto.data.DiffDto;
import com.codeit.HRBank.dto.response.CursorPageResponseChangeLogDto;
import com.codeit.HRBank.service.ChangeLogService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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


  @GetMapping("/count")
  public ResponseEntity<Long> count(
      @RequestParam(required = false) LocalDate fromDate,
      @RequestParam(required = false) LocalDate toDate
  ) {
    // 기본값 설정: fromDate = 7일 전, toDate = 현재
    if (fromDate == null) {
      fromDate = LocalDate.now().minusDays(7);
    }
    if (toDate == null) {
      toDate = LocalDate.now();
    }
    long count = changeLogService.getChangeLogCount(fromDate, toDate);
    return ResponseEntity.ok(count);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseChangeLogDto> find(
      @RequestParam(required = false) String employeeNumber,
      @RequestParam(required = false) ChangeLogType type,
      @RequestParam(required = false) String memo,
      @RequestParam(required = false) String ipAddress,
      @RequestParam(required = false) LocalDate atFrom,
      @RequestParam(required = false) LocalDate atTo,
      @RequestParam(required = false) Long idAfter,        // 이전 페이지의 마지막 ID
      @RequestParam(required = false) String cursor,       // 커서(선택)
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(defaultValue = "at") String sortField,
      @RequestParam(defaultValue = "desc") String sortDirection
  ) {
    CursorPageResponseChangeLogDto response = changeLogService.findByCondition(employeeNumber, type,
        memo, ipAddress, atFrom, atTo, idAfter, cursor, size, sortField, sortDirection);
    return ResponseEntity.
        status(HttpStatus.OK).body(response);
  }

}
