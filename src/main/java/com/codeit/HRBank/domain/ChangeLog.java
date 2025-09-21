package com.codeit.HRBank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "change_logs")
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChangeLog {

  @Id //pk
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  long id;

  @Enumerated(EnumType.STRING)
  @Column(length = 50, nullable = false)
  private ChangeLogType type; // CREATED, UPDATED, DELETED

  @Column(name = "memo", nullable = true)
  private String memo;

  @Column(name = "ip_address", nullable = false, length = 50)
  private String ipAddress;

  @CreatedDate
  @Column(name = "at", updatable = false, nullable = false)
  private LocalDateTime at;

  @Column(name = "employee_number", length = 50, nullable = false)
  private String employeeNumber;
}
