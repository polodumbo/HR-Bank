package com.codeit.HRBank.dto.data;

import com.codeit.HRBank.domain.BackupStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BackupDto(
        Long id,
        String worker,
        LocalDate startedAt,
        LocalDate endedAt,
        BackupStatus status
        , Long fileId
) {

}
