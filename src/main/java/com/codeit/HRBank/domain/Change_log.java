package com.codeit.HRBank.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "change_logs")
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Change_log {

  @Id //pk
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  long id;
  @Column(name = "type", nullable = false, length = 50)
  private String type;
  @Column(name = "memo", nullable = true)
  private String memo;
  @Column(name = "ip_address", nullable = false, length = 50)
  private String ip_address;
  @CreatedDate
  @Column(name = "at", updatable = false, nullable = false)
  private Instant at;
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(
      name = "employee_number",
      referencedColumnName = "employee_number",
      foreignKey = @ForeignKey(name = "change_logs_employees_emp_no_fk")
  )
  private Employee employee;  //employee_number
}
