package com.codeit.HRBank.dto.response;

import java.util.List;

public record CursorPageResponseChangeLogDto<T>(
    List<T> content,
    String nextCursor,
    Long nextIdAfter,
    Integer size,
    Integer totalElements,
    Boolean hasNext
) {

}
