package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Department;
<<<<<<< HEAD
import java.util.Optional;
=======
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
>>>>>>> main
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

  boolean existsByName(String name);

<<<<<<< HEAD
  Optional<Department> findByName(String name);
=======

    @Query("""
                SELECT d FROM Department d
                WHERE (:idAfter IS NULL OR d.id > :idAfter)
                AND d.name LIKE '%' || COALESCE(:nameOrDescription, "") || '%' OR d.description LIKE '%' || COALESCE(:nameOrDescription, "") || '%'
            """)
    Slice<Department> findByCondition(
            @Param("idAfter") Long idAfter,
            @Param("nameOrDescription") String nameOrDescription,
            Pageable pageable
    );
>>>>>>> main
}
