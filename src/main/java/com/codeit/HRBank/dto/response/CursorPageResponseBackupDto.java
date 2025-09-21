package com.codeit.HRBank.dto.response;

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
