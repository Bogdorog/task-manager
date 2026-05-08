package com.sergeev.taskmanager.company.internal.mapper;

import com.sergeev.taskmanager.company.api.dto.CompanyDto;
import com.sergeev.taskmanager.company.internal.entity.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyDto toResponse(Company company);
}
