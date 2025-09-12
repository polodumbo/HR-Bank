package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Department;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.domain.EmploymentStatus;
import com.codeit.HRBank.dto.data.EmployeeTrendDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    Optional<Employee> findByEmail(String email);
    Long countByEmployeeNumberStartingWith(String employeeNumber);
    boolean existsByEmail(String email);


    @Query("""
    SELECT e FROM Employee e
    WHERE (:nameOrEmail IS NULL OR e.name LIKE %:nameOrEmail% OR e.email LIKE %:nameOrEmail%)
    AND (:departmentName IS NULL OR e.department.name LIKE %:departmentName%)
    AND (:position IS NULL OR e.position LIKE %:position%)
    AND (:employeeNumber IS NULL OR e.employeeNumber LIKE %:employeeNumber%)
    AND e.hireDate >= COALESCE(:hireDateFrom, e.hireDate)
    AND e.hireDate <= COALESCE(:hireDateTo, e.hireDate)
    AND (:status IS NULL OR e.status = :status)
""")
    Slice<Employee> findByCondition(
            @Param("nameOrEmail") String nameOrEmail,
            @Param("employeeNumber") String employeeNumber,
            @Param("departmentName") String departmentName,
            @Param("position") String position,
            @Param("hireDateFrom") LocalDate hireDateFrom,
            @Param("hireDateTo") LocalDate hireDateTo,
            @Param("status") EmploymentStatus status,
            @Param("idAfter") Long idAfter,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(e) FROM Employee e
    WHERE e.hireDate >= COALESCE(:fromDate, e.hireDate)
    AND e.hireDate <= COALESCE(:toDate, e.hireDate)
    AND :status IS NULL OR e.status = :status
""")
    Long countByCondition(
            @Param("status") EmploymentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);



    @Query(value = """
        SELECT
            CAST(DATE_TRUNC(:unit, e.hire_date) AS text),
            COUNT(e.id)
        FROM
            employees e
        WHERE
            e.hire_date >= :from AND e.hire_date <= :to
        GROUP BY
            DATE_TRUNC(:unit, e.hire_date)
        ORDER BY
            DATE_TRUNC(:unit, e.hire_date)
    """, nativeQuery = true)
    List<Object[]> getTrend(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("unit") String unit
    );


}
