package com.codeit.HRBank.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "backups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Backup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(columnDefinition = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false, length = 50)
    private String worker;

    @CreatedDate
    @Column(name = "started_at", columnDefinition = "timestamp with time zone", updatable = false, nullable = false)
    private LocalDateTime startedAt;

    //    @LastModifiedDate
    @Column(columnDefinition = "timestamp with time zone", name = "ended_at")
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BackupStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "file_id",
        foreignKey = @ForeignKey(name = "backups_files_id_fk"), nullable = true)
    private File file;

    public Backup(String worker, BackupStatus status) {
        this.worker = worker;
        this.status = status;
    }
//    public Backup(String worker, BackupStatus status ) {
//        this.worker = worker;
//        this.status = status;
//    }


}
