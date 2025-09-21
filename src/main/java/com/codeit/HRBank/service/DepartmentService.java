package com.codeit.HRBank.service;

import com.codeit.HRBank.domain.Department;
import com.codeit.HRBank.dto.data.DepartmentDto;
import com.codeit.HRBank.dto.request.DepartmentCreateRequest;
import com.codeit.HRBank.dto.request.DepartmentUpdateRequest;
import com.codeit.HRBank.dto.response.CursorPageResponseDepartmentDto;
import com.codeit.HRBank.mapper.DepartmentMapper;
import com.codeit.HRBank.repository.DepartmentRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);


    @Transactional
    public DepartmentDto create(DepartmentCreateRequest request) {
        if (departmentRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("이미 존재하는 부서명 : " + request.name());
        }
        Department savedDepartment = departmentRepository.save(
            Department.builder()
                .name(request.name())
                .description(request.description())
                .establishedDate(request.establishedDate())
                .build()
        );
        return departmentMapper.toDto(savedDepartment);
    }

    @Transactional
    public DepartmentDto update(Long id, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(id).orElseThrow(
            () -> new NoSuchElementException("부서를 찾을 수 없음 : " + id)
        );

        if (!request.name().equals(department.getName())) {
            if (departmentRepository.existsByName(request.name())) {
                throw new IllegalArgumentException("중복된 부서명 : " + request.name());
            }
            department.setName(request.name());
        }

        if (request.description() != null) {
            department.setDescription(request.description());
        }

        if (request.establishedDate() != null) {
            department.setEstablishedDate(request.establishedDate());
        }

        return departmentMapper.toDto(department);
    }

    @Transactional
    public boolean delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            return false;
        }
        departmentRepository.deleteById(id);
        return true;
    }


    @Transactional
    public DepartmentDto find(Long departmentId) {
        return departmentRepository.findById(departmentId)
            .map(departmentMapper::toDto)
            .orElseThrow(
                () -> new NoSuchElementException(
                    "Department with id" + departmentId + "notfound"));
    }

    @Transactional
    public CursorPageResponseDepartmentDto findByCondition(
        String nameOrDescription,
        Long idAfter,
        String cursor,
        Integer size,
        String sortField,
        String sortDirection) {

//        Long idAfter = request.idAfter();
//        String cursor = request.cursor();
//        int size = (request.size() != null && request.size() > 0) ? request.size() : 10;
        sortField = (sortField != null) ? sortField : "establishedDate";
        sortDirection = (sortDirection != null) ? sortDirection : "ASC";
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Sort sort = Sort.by(direction, sortField);

        log.info("sortField: {}", sortField);

        Pageable pageable = PageRequest.of(0, size, sort);

        Slice<Department> departmentSlice = departmentRepository.findByCondition(
            idAfter, nameOrDescription, pageable
        );

        return departmentMapper.toDtoSlice(departmentSlice);

    }

}
