package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.domain.EmploymentStatus;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    @Query(value = "SELECT e.employeeNumber FROM Employee e WHERE e.employeeNumber LIKE :prefix ORDER BY e.employeeNumber DESC LIMIT 1")
    Optional<String> findLastEmployeeNumberStartingWith(@Param("prefix") String prefix);

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
            AND e.status = COALESCE(:status, e.status)
        """)
    Long countByCondition(
        @Param("status") EmploymentStatus status,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate);


}


