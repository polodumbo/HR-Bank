package com.codeit.HRBank.mapper;


import com.codeit.HRBank.domain.Backup;
import com.codeit.HRBank.dto.data.BackupDto;
import com.codeit.HRBank.dto.response.CursorPageResponseBackupDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class BackupMapper {

    public BackupDto toDto(Backup backup) {
        // file이 null인 경우에 대한 처리
        Long fileId = (backup.getFile() != null) ? backup.getFile().getId() : null;

        return new BackupDto(
            backup.getId(),
            backup.getWorker(),
            backup.getStartedAt(),
            backup.getEndedAt(),
            backup.getStatus(),
            fileId
        );
    }


    // Slice<Backup>을 CursorPageResponseBackupDto<BackupDto>로 변환하는 메서드
    public CursorPageResponseBackupDto<BackupDto> toDtoSlice(Slice<Backup> backupSlice) {

        // 1. Slice<Backup>의 content를 Stream을 이용해 List<BackupDto>로 변환
        List<BackupDto> dtoList = backupSlice.getContent().stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        // 2. CursorPageResponseBackupDto에 필요한 커서 및 페이지 정보 계산
        Long nextIdAfter = null;
        if (backupSlice.hasContent() && backupSlice.hasNext()) {
            // 변환된 DTO 리스트의 마지막 요소에서 ID 값을 추출
            BackupDto lastDto = dtoList.get(dtoList.size() - 1);
            nextIdAfter = lastDto.id();
        }

        // 3. 변환된 DTO 리스트와 커서 정보를 담아 최종 응답 객체 생성
        return new CursorPageResponseBackupDto<>(
            dtoList,
            null, // nextCursor는 필요시 추가
            nextIdAfter,
            backupSlice.getSize(),
            backupSlice.getNumberOfElements(),
            backupSlice.hasNext()
        );
    }

}
