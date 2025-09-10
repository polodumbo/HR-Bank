package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

  boolean existsByName(String name);
}
