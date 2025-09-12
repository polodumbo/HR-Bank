package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Department;
import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.domain.EmploymentStatus;
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
            @Param("hireDateFrom") LocalDateTime hireDateFrom,
            @Param("hireDateTo") LocalDateTime hireDateTo,
            @Param("status") EmploymentStatus status,
            @Param("idAfter") Long idAfter,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(e) FROM Employee e
    WHERE e.hireDate >= COALESCE(:fromDate, e.hireDate)
    AND e.hireDate <= COALESCE(:toDate, e.hireDate)
    AND e.status = :status
""")
    Long countByCondition(
            @Param("status") EmploymentStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

}
