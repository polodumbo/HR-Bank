package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Department;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

  boolean existsByName(String name);


    @Query("""
                SELECT d FROM Department d
                WHERE (:idAfter IS NULL OR d.id > :idAfter)
                AND (:nameOrDescription IS NULL OR d.name LIKE '%' || :nameOrDescription || '%' OR d.description LIKE '%' || :nameOrDescription || '%' )
            """)
    Slice<Department> findByCondition(
            @Param("nameOrDescription") String nameOrDescription,
            @Param("idAfter") Long idAfter,
            Pageable pageable
    );
}
