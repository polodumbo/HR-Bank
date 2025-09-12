package com.codeit.HRBank.dto.request;

import com.codeit.HRBank.domain.BackupStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BackupFindRequest(
        String worker,
        BackupStatus status,
        LocalDate startedAtFrom,
        LocalDate startedAtTo,
        Long idAfter,
        String cursor,
        Integer size,
        String sortField,
        String sortDirection
) {

}
