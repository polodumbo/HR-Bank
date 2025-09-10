package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Employee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    Optional<Employee> findByEmail(String email);
    Long countByEmployeeNumberStartingWith(String employeeNumber);
}
