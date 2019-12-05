package com.sc.service.mapper;

import com.sc.domain.*;
import com.sc.service.dto.DepartmentDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Department} and its DTO {@link DepartmentDTO}.
 */
@Mapper(componentModel = "spring", uses = {LocationMapper.class})
public interface DepartmentMapper extends EntityMapper<DepartmentDTO, Department> {

    @Mapping(source = "location.id", target = "locationId")
    DepartmentDTO toDto(Department department);

    @Mapping(source = "locationId", target = "location")
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "removeEmployee", ignore = true)
    Department toEntity(DepartmentDTO departmentDTO);

    default Department fromId(Long id) {
        if (id == null) {
            return null;
        }
        Department department = new Department();
        department.setId(id);
        return department;
    }
}
