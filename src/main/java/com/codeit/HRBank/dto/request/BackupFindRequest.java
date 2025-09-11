package com.codeit.HRBank.dto.request;

import com.codeit.HRBank.domain.BackupStatus;
import java.time.LocalDateTime;

public record BackupFindRequest(
        String worker,
        BackupStatus status,
        LocalDateTime startedAtFrom,
        LocalDateTime startedAtTo
) {

}
