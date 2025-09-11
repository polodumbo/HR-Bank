package com.codeit.HRBank.dto.response;

import com.codeit.HRBank.dto.data.BackupDto;
import java.util.List;

public record CursorPageResponseBackupDto<T>(
        List<T> content,
        String nextCursor,
        Long nextIdAfter,
        Integer size,
        Integer totalElements,
        Boolean hasNext
) {

}
