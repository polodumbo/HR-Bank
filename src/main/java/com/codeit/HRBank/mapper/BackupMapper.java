package com.codeit.HRBank.mapper;


import com.codeit.HRBank.domain.Backup;
import com.codeit.HRBank.dto.data.BackupDto;
import org.springframework.stereotype.Component;

@Component
public class BackupMapper {

    public BackupDto toDto(Backup backup){
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
}
