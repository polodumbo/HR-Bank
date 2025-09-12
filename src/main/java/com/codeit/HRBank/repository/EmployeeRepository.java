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



//    @Query(value = """
//        SELECT
//            CAST(DATE_TRUNC(:unit, e.hire_date) AS text),
//            COUNT(e.id)
//        FROM
//            employees e
//        WHERE
//            e.hire_date >= :from AND e.hire_date <= :to
//        GROUP BY
//            DATE_TRUNC(:unit, e.hire_date)
//        ORDER BY
//            DATE_TRUNC(:unit, e.hire_date)
//    """, nativeQuery = true)
//    List<Object[]> getTrend(
//            @Param("from") LocalDate from,
//            @Param("to") LocalDate to,
//            @Param("unit") String unit
//    );

    // 부서별 분포를 조회하는 쿼리
    @Query("""
        SELECT
            e.department.name,
            COUNT(e.id) AS count,
            (CAST(COUNT(e.id) AS double) / CAST((SELECT COUNT(e2.id) FROM Employee e2 WHERE e2.status = :status) AS double)) * 100
        FROM
            Employee e
        WHERE
            e.status = :status
        GROUP BY
            e.department.name
        ORDER BY
            count DESC
    """)
    List<Object[]> getDistributionByDepartment(@Param("status") EmploymentStatus status);

    // 직급별 분포를 조회하는 쿼리
    @Query("""
        SELECT
            e.position,
            COUNT(e.id) AS count,
            (CAST(COUNT(e.id) AS double) / CAST((SELECT COUNT(e2.id) FROM Employee e2 WHERE e2.status = :status) AS double)) * 100
        FROM
            Employee e
        WHERE
            e.status = :status
        GROUP BY
            e.position
        ORDER BY
            count DESC
    """)
    List<Object[]> getDistributionByPosition(@Param("status") EmploymentStatus status);

    //trend 그냥 6개로 구현

    @Query(value = """
        SELECT
            CAST(DATE_TRUNC('day', e.hire_date) AS TEXT),
            COUNT(e.id)
        FROM
            employees e
        WHERE
            e.hire_date >= :from AND e.hire_date <= :to
        GROUP BY
            DATE_TRUNC('day', e.hire_date)
        ORDER BY
            DATE_TRUNC('day', e.hire_date)
    """, nativeQuery = true)
    List<Object[]> getTrendByDay(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query(value = """
        SELECT
            CAST(DATE_TRUNC('week', e.hire_date) AS TEXT),
            COUNT(e.id)
        FROM
            employees e
        WHERE
            e.hire_date >= :from AND e.hire_date <= :to
        GROUP BY
            DATE_TRUNC('week', e.hire_date)
        ORDER BY
            DATE_TRUNC('week', e.hire_date)
    """, nativeQuery = true)
    List<Object[]> getTrendByWeek(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query(value = """
        SELECT
            CAST(DATE_TRUNC('month', e.hire_date) AS TEXT),
            COUNT(e.id)
        FROM
            employees e
        WHERE
            e.hire_date >= :from AND e.hire_date <= :to
        GROUP BY
            DATE_TRUNC('month', e.hire_date)
        ORDER BY
            DATE_TRUNC('month', e.hire_date)
    """, nativeQuery = true)
    List<Object[]> getTrendByMonth(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query(value = """
        SELECT
            CAST(DATE_TRUNC('quarter', e.hire_date) AS TEXT),
            COUNT(e.id)
        FROM
            employees e
        WHERE
            e.hire_date >= :from AND e.hire_date <= :to
        GROUP BY
            DATE_TRUNC('quarter', e.hire_date)
        ORDER BY
            DATE_TRUNC('quarter', e.hire_date)
    """, nativeQuery = true)
    List<Object[]> getTrendByQuarter(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query(value = """
        SELECT
            CAST(DATE_TRUNC('year', e.hire_date) AS TEXT),
            COUNT(e.id)
        FROM
            employees e
        WHERE
            e.hire_date >= :from AND e.hire_date <= :to
        GROUP BY
            DATE_TRUNC('year', e.hire_date)
        ORDER BY
            DATE_TRUNC('year', e.hire_date)
    """, nativeQuery = true)
    List<Object[]> getTrendByYear(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
