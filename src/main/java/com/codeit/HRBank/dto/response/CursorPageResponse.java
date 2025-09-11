package com.codeit.HRBank.dto.response;

import java.util.List;

public record CursorPageResponse<T>(
    List<T> items,
    boolean hasNext,
    String nextCursor) {

}
