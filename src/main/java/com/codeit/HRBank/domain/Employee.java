package com.codeit.HRBank.domain;

import jakarta.persistence.CascadeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "employee_number", nullable = false, unique = true, length = 50)
    private String employeeNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false,
        foreignKey = @ForeignKey(name = "employees_departments_id_fk"))
    private Department department;

    @Column(nullable = false, length = 50)
    private String position;

    @Column(name = "hire_date", nullable = false)
    private LocalDateTime hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EmploymentStatus status;


    @ManyToOne(fetch = FetchType.LAZY, optional = true)

    @OneToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.ALL, orphanRemoval = true, nullable = true)

    @JoinColumn(name = "profile_image_id",
        foreignKey = @ForeignKey(name = "employees_files_id_fk"))
    private File profileImage;
}
